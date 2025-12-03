use std::{
    array::from_fn, fs, sync::{Arc, RwLock}
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
    global_consts: Option<Arc<Vec<Constant>>>,
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
                global_consts: None,
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
            let mut app = self.data.write().unwrap();
            app.app_file = Some(app_file.clone());
            app.global_consts = Some(Arc::new(
                app_file.get_global_consts().clone(),
            ));
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
        let app_file = { self.data.read().unwrap().app_file.clone().unwrap() };

        let arc_global_data =
            { Arc::clone(&(self.data.read().unwrap().global_consts.clone().unwrap())) };

        let mut flow = Flow::new(
            app_file.get_entry_point_key(index) as usize,
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

/// ------------------- EXECUTION FRAME -------------------

pub struct FunctionFrame {
    pub function: Function,
    pub ip: usize,
    pub registers: [Cell; 256],
    pub return_to_reg: usize,
}
impl FunctionFrame {
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
    functions_stack: Vec<FunctionFrame>,
    local_data: Vec<Constant>,
    public_data: [Constant; 256],
    global_consts: Arc<Vec<Constant>>,
}
impl Flow {
    pub fn new(
        start_point: usize,
        global_consts_arc: Arc<Vec<Constant>>,
    ) -> Self {
        let main_func = global_consts_arc
            .get(start_point)
            .expect("Main function (0) not found in heap").clone();
        let function = match main_func {
            Constant::Function(f) => f,
            _ => panic!(),
        };
        let main_frame = FunctionFrame::new(function.clone(), 0);
        Self {
            functions_stack: vec![main_frame],
            local_data: Vec::new(),
            public_data: from_fn(|_| Constant::Null),
            global_consts: global_consts_arc,
        }
    }
    #[inline(always)]
    pub fn current_frame_mut(&mut self) -> Option<&mut FunctionFrame> {
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
pub fn push_to_local_heap(vm: &mut Flow, constant: Constant) -> u64 {
    let idx = vm.local_data.len() as usize;
    vm.local_data[idx] = constant;
    idx as u64
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

fn op_to_locals(vm: &mut Flow) -> VMResult<()> {
    let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
    let dst = frame.read_u8() as usize;
    let cid = frame.read_u16() as usize;
    let val: Constant;
    {
        let gl = vm.;
        let global_data = gl.read().unwrap()
        let val_ref = global_data.get(cid).unwrap();
        val = val_ref.clone();
    };
    let val_idx = vm.local_data.len();
    vm.local_data.push(val.clone());
    frame.registers[dst] = match val {
        Constant::F64(float) => Cell::F64(float as f64),
        Constant::I64(int) => Cell::I64(int as i64),
        Constant::F64(float) => Cell::F64(val_idx as f64),
        Constant::F64(float) => Cell::F64(val_idx as f64),
        Constant::F64(float) => Cell::F64(val_idx as f64),
        Constant::F64(float) => Cell::F64(val_idx as f64),
        _ => return Err(VMError::HeapError("Types mismatch".to_string())),
    };
    Ok(())
}
fn op_to_globals(vm: &mut Flow) -> VMResult<()> {
    let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
    let dst = frame.read_u8() as usize;
    let cid = frame.read_u16() as usize;
    let val = frame.function.constants[cid];
    frame.registers[dst] = val;
    Ok(())
}

fn op_move(vm: &mut Flow) -> VMResult<()> {
    let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
    let dst = frame.read_u8() as usize;
    let src = frame.read_u8() as usize;
    frame.registers[dst] = frame.registers[src];
    Ok(())
}

fn op_add(vm: &mut Flow) -> VMResult<()> {
    let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
    let dst = frame.read_u8() as usize;
    let idx_a = frame.read_u8() as usize;
    let idx_b = frame.read_u8() as usize;

    let a = frame.registers[idx_a];
    let b = frame.registers[idx_b];

    let result = match (a, b) {
        (Cell::I64(a), Cell::I64(b)) => Cell::I64(a + b),
        (Cell::U64(a), Cell::U64(b)) => Cell::U64(a + b),
        (Cell::F64(a), Cell::F64(b)) => Cell::F64(a + b),
        (Cell::I64(a), Cell::F64(b)) => Cell::F64(a as f64 + b),
        (Cell::U64(a), Cell::F64(b)) => Cell::F64(a as f64 + b),
        (Cell::F64(a), Cell::I64(b)) => Cell::F64(a + b as f64),
        (Cell::F64(a), Cell::U64(b)) => Cell::F64(a + b as f64),
        _ => {
            return Err(VMError::FunctionError(format!(
                "Cannot add types {:?} and {:?}",
                a, b
            )))
        }
    };
    frame.registers[dst] = result;
    Ok(())
}
fn op_sub(vm: &mut Flow) -> VMResult<()> {
    let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
    let dst = frame.read_u8() as usize;
    let idx_a = frame.read_u8() as usize;
    let idx_b = frame.read_u8() as usize;

    let a = frame.registers[idx_a];
    let b = frame.registers[idx_b];

    let result = match (a, b) {
        (Cell::I64(a), Cell::I64(b)) => Cell::I64(a - b),
        (Cell::U64(a), Cell::U64(b)) => Cell::U64(a - b),
        (Cell::F64(a), Cell::F64(b)) => Cell::F64(a - b),
        (Cell::I64(a), Cell::F64(b)) => Cell::F64(a as f64 - b),
        (Cell::U64(a), Cell::F64(b)) => Cell::F64(a as f64 - b),
        (Cell::F64(a), Cell::I64(b)) => Cell::F64(a - b as f64),
        (Cell::F64(a), Cell::U64(b)) => Cell::F64(a - b as f64),
        _ => {
            return Err(VMError::FunctionError(format!(
                "Cannot subtract types {:?} and {:?}",
                a, b
            )))
        }
    };
    frame.registers[dst] = result;
    Ok(())
}

fn op_mul(vm: &mut Flow) -> VMResult<()> {
    let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
    let dst = frame.read_u8() as usize;
    let idx_a = frame.read_u8() as usize;
    let idx_b = frame.read_u8() as usize;

    let a = frame.registers[idx_a];
    let b = frame.registers[idx_b];

    let result = match (a, b) {
        (Cell::I64(a), Cell::I64(b)) => Cell::I64(a * b),
        (Cell::U64(a), Cell::U64(b)) => Cell::U64(a * b),
        (Cell::F64(a), Cell::F64(b)) => Cell::F64(a * b),
        (Cell::I64(a), Cell::F64(b)) => Cell::F64(a as f64 * b),
        (Cell::U64(a), Cell::F64(b)) => Cell::F64(a as f64 * b),
        (Cell::F64(a), Cell::I64(b)) => Cell::F64(a * b as f64),
        (Cell::F64(a), Cell::U64(b)) => Cell::F64(a * b as f64),
        _ => {
            return Err(VMError::FunctionError(format!(
                "Cannot multiply types {:?} and {:?}",
                a, b
            )))
        }
    };
    frame.registers[dst] = result;
    Ok(())
}

fn op_divide(vm: &mut Flow) -> VMResult<()> {
    let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
    let dst = frame.read_u8() as usize;
    let idx_a = frame.read_u8() as usize;
    let idx_b = frame.read_u8() as usize;

    let a = frame.registers[idx_a];
    let b = frame.registers[idx_b];

    let result = match (a, b) {
        // I64 / I64 — integer dividing (as in C, Rust, JavaScript)
        (Cell::I64(a), Cell::I64(b)) => {
            if b == 0 {
                return Err(VMError::DivisionByZero);
            }
            Cell::I64(a / b)
        }
        // F64 / F64 — IEEE 754, with NaN supporting, inf, -0.0
        (Cell::F64(a), Cell::F64(b)) => {
            if b == 0.0 {
                // 5.0 / 0.0 → inf, -5.0 / 0.0 → -inf, 0.0 / 0.0 → NaN
                // it is standard behavior
                Cell::F64(a / b)
            } else {
                Cell::F64(a / b)
            }
        }
        (Cell::U64(a), Cell::U64(b)) => {
            if b == 0 {
                return Err(VMError::DivisionByZero);
            }
            Cell::U64(a / b)
        }
        (Cell::I64(a), Cell::F64(b)) => Cell::F64(a as f64 / b),
        (Cell::U64(a), Cell::F64(b)) => Cell::F64(a as f64 / b),
        _ => {
            return Err(VMError::FunctionError(format!(
                "Cannot divide types {:?} and {:?}",
                a, b
            )));
        }
    };
    frame.registers[dst] = result;
    Ok(())
}
fn op_eq(vm: &mut Flow) -> VMResult<()> {
    let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
    let dst = frame.read_u8() as usize;
    let idx_a = frame.read_u8() as usize;
    let idx_b = frame.read_u8() as usize;

    let a = frame.registers[idx_a];
    let b = frame.registers[idx_b];

    let is_eq = match (a, b) {
        (Cell::I64(a), Cell::I64(b)) => a == b,
        (Cell::F64(a), Cell::F64(b)) => f64::to_bits(a) == f64::to_bits(b),
        (Cell::U64(a), Cell::U64(b)) => a == b,
        (Cell::Bool(a), Cell::Bool(b)) => a == b,
        (Cell::Null, Cell::Null) => true,
        (Cell::StrRef(a), Cell::StrRef(b)) => a == b,
        (Cell::ArrayRef(a), Cell::ArrayRef(b)) => a == b,
        (Cell::FuncRef(a), Cell::FuncRef(b)) => a == b,
        _ => {
            return Err(VMError::FunctionError(
                "Type mismatch in equality comparison".to_string(),
            ));
        }
    };
    frame.registers[dst] = Cell::Bool(is_eq);
    Ok(())
}

fn op_goto(vm: &mut Flow) -> VMResult<()> {
    let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
    let jump_address = frame.read_u16() as usize;
    if jump_address >= frame.function.instructions.len() {
        return Err(VMError::FunctionError(format!(
            "Jump out of bounds: {} (function size: {})",
            jump_address,
            frame.function.instructions.len()
        )));
    }
    frame.ip = jump_address;
    Ok(())
}

fn op_goto_if(vm: &mut Flow) -> VMResult<()> {
    let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
    let jump_address = frame.read_u16() as usize;
    let condition_reg = frame.read_u8() as usize;
    if jump_address >= frame.function.instructions.len() {
        return Err(VMError::FunctionError(format!(
            "Conditional jump out of bounds: {} >= {}",
            jump_address,
            frame.function.instructions.len()
        )));
    }
    let condition = frame.registers[condition_reg];
    let condition_bool = match condition {
        Cell::Bool(b) => b,
        Cell::Null => false,
        Cell::I64(0) | Cell::U64(0) | Cell::F64(0.0 | -0.0) => false,
        Cell::I64(_) | Cell::U64(_) | Cell::F64(_) => true,
        Cell::StrRef(_) | Cell::ArrayRef(_) | Cell::FuncRef(_) => true,
        _ => {
            log(Log::Warning, "Non-boolean value used as condition");
            true
        }
    };
    if condition_bool {
        frame.ip = jump_address;
    }
    Ok(())
}

fn op_call_function(vm: &mut Flow) -> VMResult<()> {
    let (func_idx, dest_reg) = {
        let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
        (frame.read_u16() as usize, frame.read_u8() as usize)
    };
    if let Some(func) = vm.global_consts.read().unwrap().get(func_idx) {
        match func {
            Constant::Function(function) => {
                let new_frame = FunctionFrame::new(function.clone(), dest_reg);
                vm.functions_stack.push(new_frame);
                Ok(())
            }
            _ => Err(VMError::FunctionError("It is not function".to_string())),
        }
    } else {
        Err(VMError::UnknownFunction(func_idx))
    }
}

fn op_return(vm: &mut Flow) -> VMResult<()> {
    let (ret_val, dest_reg) = {
        let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
        let src_reg = frame.read_u8() as usize;
        (frame.registers[src_reg].clone(), frame.return_to_reg)
    };
    vm.functions_stack.pop();
    if let Some(parent_frame) = vm.functions_stack.last_mut() {
        parent_frame.registers[dest_reg] = ret_val;
    } else {
        log(Log::Info, "Returned from top-level function.");
    }
    Ok(())
}

fn op_new_array(vm: &mut Flow) -> VMResult<()> {
    let (dst_reg, size) = {
        let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
        let dst = frame.read_u8() as usize;
        let sz = frame.read_u16() as usize;
        (dst, sz)
    };
    let new_arr = vec![Cell::Null; size];
    let arr_idx = vm.local_data.len();
    vm.local_data.push(Constant::Array(new_arr));
    let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
    frame.registers[dst_reg] = Cell::ArrayRef(arr_idx as u64);
    Ok(())
}
fn op_new_array(vm: &mut Flow) -> VMResult<()> {
    let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
    let dst_reg = frame.read_u8() as usize;
    let size = frame.read_u16() as usize;

    let array = vec![Cell::Null; size];
    let idx_in_local_heap = vm.local_data.len();
    vm.local_data.push(Constant::Array(array));

    frame.registers[dst_reg] = Cell::ArrayRef(idx_in_local_heap as u64);
    Ok(())
}

fn op_call_native(vm: &mut Flow) -> VMResult<()> {
    let frame = vm.current_frame_mut().ok_or(VMError::StackUnderflow)?;
    let _plugin_id = frame.read_u8();
    let _fun_id = frame.read_u8();
    let _args_reg = frame.read_u8();
    println!("CALL_NATIVE: Not implemented yet");
    Ok(())
}

/// ------------------- DISPATCH TABLE -------------------

const fn build_dispatch_table() -> [OpHandler; 256] {
    let mut table = [invalid_op as OpHandler; 256];

    table[Op::LoadConst as usize] = op_load_from_globals;
    table[Op::Move as usize] = op_move;
    table[Op::Add as usize] = op_add;
    table[Op::Subtract as usize] = op_sub;
    table[Op::Multiply as usize] = op_mul;
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
