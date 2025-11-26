use slotmap::{DefaultKey, Key, SlotMap};
use std::{
    fs,
    sync::{Arc, Mutex, RwLock},
};
use tokio::task::JoinHandle;

use crate::{
    app_file::AppFile,
    center::get_virtel_center,
    data::{Cell, Constant, Function},
    log::{log, Log},
    permissions::Permissions,
    tokio_setup::get_tokio,
};

use bincode::{Decode, Encode};
use serde::{Deserialize, Serialize};

/// ------------------ Error type ----------------
#[derive(Debug, Clone)]
pub enum VMError {
    StackUnderflow,
    FrameError,
    DivisionByZero,
    InvalidOpcode,
    HeapError(String),
    UnknownFunction(usize),
    FunctionError(String), // error on user side
    NativeReferencesCastingError(String),
}
pub type VMResult<T> = Result<T, VMError>;

/// ------------------- APP STRUCTURE -------------------

#[derive(Debug)]
pub enum AppStatus {
    Running,
    Background,
    Stopped,
    Paused,
    Error(String),
}
#[derive(Debug, Encode, Decode, Serialize, Deserialize)]
pub struct AppConfig {
    id: String,
    name: String,
    version: String,
    icon_path: String,
}
pub struct App {
    app_config: AppConfig,     // config.json
    app_file: Option<AppFile>, // {app_id}.vxc
    status: AppStatus,
    global_data: Option<Arc<RwLock<SlotMap<DefaultKey, Constant>>>>,
    permissions: Permissions,
    threads: Vec<JoinHandle<()>>,
}
pub struct AppElement {
    data: RwLock<App>,
}
impl AppElement {
    pub fn new(app_id: String) -> Self {
        let apps_dir = get_virtel_center()
            .get_settings()
            .filesystem
            .apps_dir
            .clone();

        let this_app_config = format!("{}/{}/config.json", apps_dir, app_id);
        let this_app_config_content = fs::read_to_string(this_app_config).unwrap();
        let app_config: AppConfig = serde_json::from_str(this_app_config_content.as_str()).unwrap();

        Self {
            data: RwLock::new(App {
                app_config: app_config,
                app_file: None,
                status: AppStatus::Stopped,
                global_data: None,
                permissions: Permissions::new(),
                threads: Vec::new(),
            }),
        }
    }
    pub fn get_id(&self) -> String {
        self.data.read().unwrap().app_config.id.clone()
    }
    pub fn get_name(&self) -> String {
        self.data.read().unwrap().app_config.name.clone()
    }
    pub fn get_version(&self) -> String {
        self.data.read().unwrap().app_config.version.clone()
    }
    fn set_status(&self, status: AppStatus) {
        self.data.write().unwrap().status = status
    }
    pub fn load_code_from_disk(&self) {
        let apps_dir = get_virtel_center()
            .get_settings()
            .filesystem
            .apps_dir
            .clone();

        let app_id = self.get_id();

        let app_file_path = format!("{}/{}/{}.vxc", apps_dir, app_id, app_id);
        let app_file_content = fs::read(&app_file_path).expect("Failed to read .vxc file");
        let bytes = app_file_content;
        let app_file = AppFile::from_bytes(bytes);
        {
            let app = self.data.write().unwrap();
            app.app_file = Some(app_file);
            app.global_data = Some(app_file.get_global_data_initial_state())
        }
    }

    pub fn on_create(&self) {
        self.load_code_from_disk();

        log(
            Log::Info,
            format!("App created: {}", self.get_id()).as_str(),
        );
        self.set_status(AppStatus::Running);

        self.start_flow_from_function(0);
    }
    pub fn on_destroy(&self) {
        println!("App {} destroyed.", self.get_id());
        self.set_status(AppStatus::Stopped);
    }
    pub fn start_flow_from_function(&self, index: usize) {
        let app_file = { self.data.read().unwrap().app_file.unwrap() };

        let arc_global_data = { Arc::clone(&(self.data.read().unwrap().global_data.unwrap())) };

        let flow = Flow::new(
            app_file.get_entry_point_key(index),
            SlotMap::new(),
            arc_global_data,
        );

        let handle = get_tokio().spawn(async move {
            if let Err(e) = flow.run() {
                println!("CRITICAL VX VM ERROR: {:?}", e);
                // TODO(Normal e handler)
            }
        });
        {
            self.data.write().unwrap().threads.push(handle);
        }
    }
    pub fn install_app(path_to_lpp: String) {
        todo!();
    }
}

/// ------------------- FUNCTIONS -------------------

/// ------------------- EXECUTION FRAME -------------------

pub struct FunctionFunnel {
    pub function: Function,
    pub ip: usize,
    pub registers: [Cell; 256],
    pub return_to_reg: usize,
}
impl FunctionFunnel {
    pub fn new(func: Function, return_to_reg: usize) -> Self {
        Self {
            function: func,
            ip: 0,
            registers: [Cell::Null; 256],
            return_to_reg,
        }
    }
    #[inline(always)]
    pub fn read_u8(&mut self) -> u8 {
        if self.ip >= self.function.instructions.len() {
            return 0;
        }
        let b = self.function.instructions[self.ip];
        self.ip += 1;
        b
    }
    #[inline(always)]
    pub fn read_u16(&mut self) -> u16 {
        let hi = self.read_u8() as u16;
        let lo = self.read_u8() as u16;
        (hi << 8) | lo
    }
}

/// ------------------- VM (FLOW) -------------------

pub struct Flow {
    functions_stack: Vec<FunctionFunnel>,
    local_data: SlotMap<DefaultKey, Constant>,
    global_data: Arc<RwLock<SlotMap<DefaultKey, Constant>>>,
}
impl Flow {
    pub fn new(
        start_key: DefaultKey,
        local_data: SlotMap<DefaultKey, Constant>,
        global_data: Arc<RwLock<SlotMap<DefaultKey, Constant>>>,
    ) -> Self {
        let main_func = local_data
            .get(start_key)
            .expect("Main function (0) not found in heap");
        let function = match main_func {
            Constant::Function(f) => f,
            _ => panic!(),
        };
        let main_frame = FunctionFunnel::new(function.clone(), 0);
        Self {
            functions_stack: vec![main_frame],
            local_data,
            global_data,
        }
    }
    #[inline(always)]
    pub fn current_frame_mut(&mut self) -> Option<&mut FunctionFunnel> {
        self.functions_stack.last_mut()
    }
    pub fn run(&mut self) -> VMResult<()> {
        while !self.functions_stack.is_empty() {
            let handler = DISPATCH_TABLE[self.current_frame_mut().unwrap().read_u8() as usize];
            handler(self)?
        }
        Ok(())
    }
}

/// ------------------- OPCODES & HANDLERS -------------------

#[repr(u8)]
#[derive(Debug, Clone, Copy)]
pub enum Op {
    LoadConst = 0,
    Move = 1,
    Add = 2,
    Subtract = 3,
    Multiply = 4,
    Divide = 5,
    LowerThan = 6,
    GreaterThan = 7,
    Eq = 8,
    Or = 9,
    Not = 10,
    Goto = 11,
    GotoIf = 12,
    NewFunction = 13,
    CallFunction = 14,
    Return = 15,
    NewArray = 16,
    GetArrayItem = 17,
    SetArrayItem = 18,
    GetArrayLength = 19,
    IsType = 20,

    SystemWrite,
    CallNative,
}

pub type OpHandler = fn(&mut Flow) -> VMResult<()>;

fn invalid_op(_vm: &mut Flow) -> VMResult<()> {
    Err(VMError::InvalidOpcode)
}

fn op_load_const(vm: &mut Flow) {
    let frame = vm.current_frame_mut();
    let dst = frame.read_u8() as usize;
    let cid = frame.read_u16() as usize;
    // Копируем константу из таблицы работы функции в регистр
    let val = frame.function.worktable.constants[cid];
    frame.registers[dst] = val;
}

fn op_move(vm: &mut Flow) {
    let frame = vm.current_frame_mut();
    let dst = frame.read_u8() as usize;
    let src = frame.read_u8() as usize;
    frame.registers[dst] = frame.registers[src];
}

fn op_add(vm: &mut Flow) {
    let frame = vm.current_frame_mut();
    let dst = frame.read_u8() as usize;
    let idx_a = frame.read_u8() as usize;
    let idx_b = frame.read_u8() as usize;

    let a = frame.registers[idx_a].as_i64();
    let b = frame.registers[idx_b].as_i64();

    frame.registers[dst] = ValueOrRef::new_i64(a + b);
}

fn op_subtract(vm: &mut Flow) {
    let frame = vm.current_frame_mut();
    let dst = frame.read_u8() as usize;
    let idx_a = frame.read_u8() as usize;
    let idx_b = frame.read_u8() as usize;

    let a = frame.registers[idx_a].as_i64();
    let b = frame.registers[idx_b].as_i64();

    frame.registers[dst] = ValueOrRef::new_i64(a - b);
}

fn op_multiply(vm: &mut Flow) {
    let frame = vm.current_frame_mut();
    let dst = frame.read_u8() as usize;
    let idx_a = frame.read_u8() as usize;
    let idx_b = frame.read_u8() as usize;

    let a = frame.registers[idx_a].as_i64();
    let b = frame.registers[idx_b].as_i64();

    frame.registers[dst] = ValueOrRef::new_i64(a * b);
}

fn op_divide(vm: &mut Flow) -> VMResult<()> {
    let frame: FunctionFunnel = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
    let dst = frame.read_u8() as usize;
    let idx_a = frame.read_u8() as usize;
    let idx_b = frame.read_u8() as usize;

    let a = frame.registers[idx_a].as_i64();
    let b = frame.registers[idx_b].as_i64();

    if b == 0 {
        return Err(VMError::DivisionByZero);
    }
    frame.registers[dst] = ValueOrRef::new_i64(a / b);
    Ok(())
}

fn op_eq(vm: &mut Flow) {
    let frame = vm.current_frame_mut();
    let dst = frame.read_u8() as usize;
    let idx_a = frame.read_u8() as usize;
    let idx_b = frame.read_u8() as usize;

    let a = frame.registers[idx_a];
    let b = frame.registers[idx_b];

    // Simple equality
    let is_eq = a.typ == b.typ && a.bits == b.bits;

    frame.registers[dst] = ValueOrRef::new_bool(is_eq);
}

fn op_goto(vm: &mut Flow) {
    let frame = vm.current_frame_mut();
    // Читаем адрес (куда прыгать). Обычно это u16 или u32
    let jump_address = frame.read_u16() as usize;

    // Просто меняем Instruction Pointer
    frame.ip = jump_address;
}

fn op_goto_if(vm: &mut Flow) {
    let frame = vm.current_frame_mut();
    let jump_address = frame.read_u16() as usize;
    let condition_reg = frame.read_u8() as usize;
    let condition = frame.registers[condition_reg];
    if condition.as_bool() {
        frame.ip = jump_address;
    }
}

fn op_call_function(vm: &mut Flow) -> VMResult<()> {
    let (func_idx, dest_reg) = {
        let frame: FunctionFunnel = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
        (frame.read_u16() as usize, frame.read_u8() as usize)
    };
    if let Some(func) = vm.heap.get_function(func_idx) {
        let new_frame = FunctionFunnel::new(func.clone(), dest_reg);
        vm.functions_stack.push(new_frame);
        Ok(())
    } else {
        Err(VMError::UnknownFunction(func_idx))
    }
}
fn op_return(vm: &mut Flow) {
    let (ret_val, dest_reg) = {
        let frame = vm.current_frame_mut();
        let src_reg = frame.read_u8() as usize;
        (frame.registers[src_reg], frame.return_to_reg)
    };
    vm.functions_stack.pop();
    if let Some(parent_frame) = vm.functions_stack.last_mut() {
        parent_frame.registers[dest_reg] = ret_val;
    }
}

fn op_new_array(vm: &mut Flow) -> VMResult<()> {
    let (dst_reg, size) = {
        let frame: FunctionFunnel = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
        let dst = frame.read_u8() as usize;
        let sz = frame.read_u16() as usize;
        (dst, sz)
    };
    let new_arr = vec![ValueOrRef::new_null(); size];
    let arr_idx = vm.heap.alloc_array(new_arr);
    let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
    frame.registers[dst_reg] = ValueOrRef::new_array_ref(arr_idx);
    Ok(())
}

fn op_call_native(vm: &mut Flow) {
    let frame = vm.current_frame_mut();
    let _plugin_id = frame.read_u8();
    let _fun_id = frame.read_u8();
    let _args_reg = frame.read_u8();
    println!("CALL_NATIVE: Not implemented yet");
}

/// ------------------- DISPATCH TABLE -------------------

const fn build_dispatch_table() -> [OpHandler; 256] {
    let mut table = [invalid_op as OpHandler; 256];

    table[Op::LoadConst as usize] = op_load_const;
    table[Op::Move as usize] = op_move;
    table[Op::Add as usize] = op_add;
    table[Op::Subtract as usize] = op_subtract;
    table[Op::Multiply as usize] = op_multiply;
    table[Op::Divide as usize] = op_divide;

    table[Op::Eq as usize] = op_eq;

    table[Op::CallFunction as usize] = op_call_function;
    table[Op::Return as usize] = op_return;
    table[Op::CallNative as usize] = op_call_native;

    table
}

static DISPATCH_TABLE: [OpHandler; 256] = build_dispatch_table();

#[cfg(test)]
mod tests {
    use crate::app::Op;

    use super::*;
    #[test]
    fn it_works() {
        let app = AppElement::new("vladceresna.virtel.launcher".to_string());

        app.on_create();

        app.on_destroy();

        let result = add(2, 2);
        assert_eq!(result, 4);
    }
}
