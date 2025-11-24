use slotmap::SlotMap;
use std::{
    fs,
    sync::{Arc, Mutex, RwLock},
};
use tokio::task::JoinHandle;

use crate::{
    center::get_virtel_center,
    chunk_utils::{app_heap_object_deserialize, vc_to_heap},
    data::{Cell, Function},
    log::log,
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
    InvalidOpcode(u8),
    HeapError(String),
    UnknownFunction(usize),
    FunctionError(String), // error on user side
}
pub type VMResult<T> = Result<T, VMError>;

/// ------------------- HEAP -------------------

pub struct Heap {
    pub strings: SlotMap<String>,
    pub functions: SlotMap<Function>,
    pub arrays: SlotMap<Cell>,
}

/// ------------------- APP STRUCTURE -------------------

#[derive(Debug)]
pub enum AppStatus {
    Running,
    Background,
    Stopped,
    Paused,
    Error(String),
}
impl Default for AppStatus {
    fn default() -> Self {
        AppStatus::Stopped
    }
}
struct AppData {
    id: String,
    name: String,
    version: String,
    icon_path: String,
    status: AppStatus,
    permissions: Permissions,
    threads: Vec<JoinHandle<()>>,
    heap: Heap,
}
pub struct App {
    data: Mutex<AppData>,
}
impl App {
    pub fn new(app_id: String) -> Self {
        let apps_dir = get_virtel_center()
            .get_settings()
            .filesystem
            .apps_dir
            .clone();
        let this_app_config = format!("{}/{}/config.json", apps_dir, app_id);
        let this_app_config_content = fs::read_to_string(this_app_config).unwrap();

        let c: serde_json::Value = serde_json::from_str(this_app_config_content.as_str()).unwrap();

        Self {
            data: Mutex::new(AppData {
                id: app_id,
                name: c["name"].to_string(),
                version: c["version"].to_string(),
                icon_path: c["iconPath"].to_string(),
                status: AppStatus::default(),
                permissions: Permissions::new(),
                threads: Vec::new(),
                heap: Heap::new(),
            }),
        }
    }
    pub fn get_id(&self) -> String {
        self.data.lock().unwrap().id.clone()
    }
    pub fn get_name(&self) -> String {
        self.data.lock().unwrap().name.clone()
    }
    pub fn get_version(&self) -> String {
        self.data.lock().unwrap().version.clone()
    }
    pub fn on_create(&self) {
        let apps_dir = get_virtel_center()
            .get_settings()
            .filesystem
            .apps_dir
            .clone();
        let app_id = self.data.lock().unwrap().id.clone();
        let app_file = format!("{}/{}/code/{}.vc", apps_dir, app_id, app_id);

        let vc = fs::read(&app_file).expect("Failed to read .vc file");
        let heap = Arc::new(app_heap_object_deserialize(vc_to_heap(vc)));

        log(
            crate::log::Log::Info,
            format!("App created: {}", app_id).as_str(),
        );
        self.data.lock().unwrap().status = AppStatus::Running;

        let handle = get_tokio().spawn(async move {
            let mut vm = Flow::new(heap);
            if let Err(e) = vm.run() {
                println!("CRITICAL VX VM ERROR: {:?}", e);
            }
        });
        self.data.lock().unwrap().threads.push(handle);
    }
    pub fn on_destroy(&self) {
        println!("App {} destroyed.", self.data.lock().unwrap().id);
        self.data.lock().unwrap().status = AppStatus::Stopped;
    }
}

/// ------------------- FUNCTIONS -------------------

/// ------------------- EXECUTION FRAME -------------------

pub struct FunctionFunnel {
    pub function: &Function,
    pub ip: usize,
    pub registers: [Cell; 256],
    pub return_to_reg: usize,
}
impl FunctionFunnel {
    pub fn new(func: &Function, return_to_reg: usize) -> Self {
        Self {
            function: func,
            ip: 0,
            registers: [ValueOrRef::new_null(); 256],
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
    pub functions_stack: Vec<FunctionFunnel>,
    pub heap: Arc<Heap>,
}
impl Flow {
    pub fn new(heap: Arc<Heap>) -> Self {
        let main_func = heap
            .get_function(0)
            .expect("Main function (0) not found in heap");
        let main_frame = FunctionFunnel::new(main_func, 0);
        Self {
            functions_stack: vec![main_frame],
            heap,
        }
    }
    #[inline(always)]
    pub fn current_frame_mut(&mut self) -> Option<&mut FunctionFunnel> {
        self.functions_stack.last_mut()
    }
    pub fn run(&mut self) -> VMResult<()> {
        while !self.functions_stack.is_empty() {
            let handler = DISPATCH_TABLE[self.current_frame_mut().read_u8() as usize];
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
