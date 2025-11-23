use std::{
    fs,
    sync::{Arc, Mutex, RwLock},
};
use tokio::task::JoinHandle;

use crate::{
    center::get_virtel_center,
    chunk_utils::{heap_to_deserialized, vc_to_heap},
    log::log,
    permissions::Permissions,
    tokio_setup::get_tokio,
};

use bincode::{Decode, Encode};
use serde::{Deserialize, Serialize};

/// ------------------- TYPES & VALUES -------------------

#[derive(Debug, Copy, PartialEq, Clone, Encode, Decode, Serialize, Deserialize)]
pub enum ValueOrRefType {
    /// 8-bit unsigned integer (0 … 255).
    Byte,
    /// 64-bit signed integer.
    I64,
    /// 64-bit floating-point number.
    F64,
    /// Boolean.
    Bool,
    /// Index into string table.
    StrRef,
    /// Index into array table.
    ArrayRef,
    /// Index into function table.
    FuncRef,
    /// Null value.
    Null,
}

#[derive(Debug, Copy, PartialEq, Clone, Encode, Decode, Serialize, Deserialize)]
pub struct ValueOrRef {
    pub typ: ValueOrRefType,
    pub bits: u64,
}
impl ValueOrRef {
    pub fn type_is(&self, compared_typ: ValueOrRefType) -> bool {
        self.typ == compared_typ
    }
    pub fn new_null() -> Self {
        ValueOrRef {
            typ: ValueOrRefType::Null,
            bits: 0,
        }
    }
    pub fn new_i64(v: i64) -> Self {
        ValueOrRef {
            typ: ValueOrRefType::I64,
            bits: v as u64,
        }
    }
    pub fn as_i64(&self) -> i64 {
        self.bits as i64
    }
    pub fn new_f64(v: f64) -> Self {
        ValueOrRef {
            typ: ValueOrRefType::F64,
            bits: f64::to_bits(v),
        }
    }
    pub fn as_f64(&self) -> f64 {
        f64::from_bits(self.bits)
    }
    pub fn new_bool(v: bool) -> Self {
        ValueOrRef {
            typ: ValueOrRefType::Bool,
            bits: v as u64,
        }
    }
    pub fn as_bool(&self) -> bool {
        self.bits != 0
    }
    pub fn new_byte(v: u8) -> Self {
        ValueOrRef {
            typ: ValueOrRefType::Byte,
            bits: v as u64,
        }
    }
    pub fn as_byte(&self) -> u8 {
        self.bits as u8
    }
    pub fn new_str_ref(idx: u64) -> Self {
        ValueOrRef {
            typ: ValueOrRefType::StrRef,
            bits: idx,
        }
    }
    pub fn as_str_ref(&self) -> u64 {
        self.bits
    }
    pub fn new_array_ref(idx: u64) -> Self {
        ValueOrRef {
            typ: ValueOrRefType::ArrayRef,
            bits: idx,
        }
    }
    pub fn as_array_ref(&self) -> u64 {
        self.bits
    }
    pub fn new_func_ref(idx: u64) -> Self {
        ValueOrRef {
            typ: ValueOrRefType::FuncRef,
            bits: idx,
        }
    }
    pub fn as_func_ref(&self) -> u64 {
        self.bits
    }
}

/// ------------------- HEAP -------------------

pub struct AppHeap {
    pub strings: Arc<RwLock<Vec<String>>>,
    pub arrays: Arc<RwLock<Vec<Vec<ValueOrRef>>>>,
    pub functions: Arc<RwLock<Vec<Function>>>,
}

impl AppHeap {
    pub fn new() -> Self {
        Self {
            strings: Arc::new(RwLock::new(Vec::new())),
            arrays: Arc::new(RwLock::new(Vec::new())),
            functions: Arc::new(RwLock::new(Vec::new())),
        }
    }
    pub fn alloc_string(&self, s: String) -> usize {
        let mut strings = self.strings.write().unwrap();
        let index = strings.len();
        strings.push(s);
        index
    }
    pub fn get_string(&self, index: usize) -> Option<String> {
        let strings = self.strings.read().unwrap();
        strings.get(index).cloned()
    }
    pub fn alloc_array(&self, arr: Vec<ValueOrRef>) -> usize {
        let mut arrays = self.arrays.write().unwrap();
        let index = arrays.len();
        arrays.push(arr);
        index
    }
    pub fn get_array(&self, index: usize) -> Option<Vec<ValueOrRef>> {
        let arrays = self.arrays.read().unwrap();
        arrays.get(index).cloned()
    }
    pub fn array_set_at(
        &self,
        array_idx: usize,
        item_idx: usize,
        value: ValueOrRef,
    ) -> Result<(), String> {
        let mut arrays = self.arrays.write().unwrap();
        if let Some(arr) = arrays.get_mut(array_idx) {
            if item_idx < arr.len() {
                arr[item_idx] = value;
                Ok(())
            } else {
                Err(format!("Array index out of bounds: {}", item_idx))
            }
        } else {
            Err(format!("Heap array not found: {}", array_idx))
        }
    }
    pub fn alloc_function(&self, func: Function) -> usize {
        let mut functions = self.functions.write().unwrap();
        let index = functions.len();
        functions.push(func);
        index
    }
    pub fn get_function(&self, index: usize) -> Option<Function> {
        let functions = self.functions.read().unwrap();
        functions.get(index).cloned()
    }
}

#[derive(Debug, Clone, Encode, Decode, Serialize, Deserialize)]
pub struct AppHeapSerialized {
    pub strings: Vec<String>,
    pub arrays: Vec<Vec<ValueOrRef>>,
    pub functions: Vec<Function>,
}

/// ------------------- APP STRUCTURE -------------------

#[derive(Debug)]
pub enum AppStatus {
    Running,
    Background,
    Stopped,
    Paused,
    Error,
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
    heap: AppHeap,
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
                heap: AppHeap::new(),
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
        let heap = Arc::new(heap_to_deserialized(vc_to_heap(vc)));

        log(
            crate::log::Log::Info,
            format!("App created: {}", app_id).as_str(),
        );
        self.data.lock().unwrap().status = AppStatus::Running;

        let handle = get_tokio().spawn(async move {
            let mut vm = Flow::new(heap);
            vm.run();
        });
        self.data.lock().unwrap().threads.push(handle);
    }
    pub fn on_destroy(&self) {
        println!("App {} destroyed.", self.data.lock().unwrap().id);
        self.data.lock().unwrap().status = AppStatus::Stopped;
    }
}

/// ------------------- FUNCTIONS -------------------

#[derive(Debug, Clone, Serialize, Deserialize, Encode, Decode)]
pub struct FunctionSignature {
    pub args: Vec<ValueOrRefType>,
    pub result_type: ValueOrRefType,
}

#[derive(Debug, Clone, Serialize, Deserialize, Encode, Decode)]
pub struct FunctionWorkTable {
    pub constants: Vec<ValueOrRef>,
}

#[derive(Debug, Clone, Serialize, Deserialize, Encode, Decode)]
pub enum FunctionResultType {
    Success,
    Warning,
    Failure,
}
#[derive(Debug, Clone, Serialize, Deserialize, Encode, Decode)]
pub struct FunctionResult {
    typ: FunctionResultType,
    error_message: String,
    data: ValueOrRef,
}

#[derive(Debug, Clone, Serialize, Deserialize, Encode, Decode)]
pub struct Function {
    pub signature: FunctionSignature,
    pub worktable: FunctionWorkTable,
    pub result: Option<FunctionResult>,
    pub instructions: Vec<u8>,
}
impl Function {
    pub fn new(args: Vec<ValueOrRefType>, result_type: ValueOrRefType) -> Self {
        Self {
            signature: FunctionSignature { args, result_type },
            worktable: FunctionWorkTable {
                constants: Vec::new(),
            },
            result: None,
            instructions: Vec::new(),
        }
    }
}

/// ------------------- EXECUTION FRAME -------------------

pub struct FunctionFunnel {
    pub function: Function,
    pub ip: usize,
    pub registers: [ValueOrRef; 256],
    pub return_to_reg: usize,
}
impl FunctionFunnel {
    pub fn new(func: Function, return_to_reg: usize) -> Self {
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
    pub heap: Arc<AppHeap>,
}
impl Flow {
    pub fn new(heap: Arc<AppHeap>) -> Self {
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
    pub fn current_frame_mut(&mut self) -> &mut FunctionFunnel {
        let last_idx = self.functions_stack.len() - 1;
        &mut self.functions_stack[last_idx]
    }
    pub fn run(&mut self) {
        while !self.functions_stack.is_empty() {
            let handler = DISPATCH_TABLE[self.current_frame_mut().read_u8() as usize];
            handler(self);
        }
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

pub type OpHandler = fn(&mut Flow);

fn invalid_op(_vm: &mut Flow) {
    panic!("Invalid or unimplemented opcode");
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

fn op_divide(vm: &mut Flow) {
    let frame = vm.current_frame_mut();
    let dst = frame.read_u8() as usize;
    let idx_a = frame.read_u8() as usize;
    let idx_b = frame.read_u8() as usize;

    let a = frame.registers[idx_a].as_i64();
    let b = frame.registers[idx_b].as_i64();

    if b == 0 {
        panic!("Runtime Error: Division by zero");
    }

    frame.registers[dst] = ValueOrRef::new_i64(a / b);
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

    // Если true, прыгаем. Если false — просто идем дальше.
    if condition.as_bool() {
        frame.ip = jump_address;
    }
}

fn op_call_function(vm: &mut Flow) {
    let (func_idx, dest_reg) = {
        let frame = vm.current_frame_mut();
        (frame.read_u16() as usize, frame.read_u8() as usize)
    };
    if let Some(func) = vm.heap.get_function(func_idx) {
        let new_frame = FunctionFunnel::new(func, dest_reg);
        vm.functions_stack.push(new_frame);
    } else {
        panic!("Runtime Error: CallFunction ID {} not found", func_idx);
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

fn op_new_array(vm: &mut Flow) {
    let frame = vm.current_frame_mut();
    let dst_reg = frame.read_u8() as usize;
    let size = frame.read_u16() as usize; // Или читать размер из другого регистра

    // Создаем вектор с Null
    let new_arr = vec![ValueOrRef::new_null(); size];

    // Аллоцируем в куче (Heap)
    // ВАЖНО: Тут нужно освободить borrow frame, чтобы получить доступ к heap
    // Поэтому сначала читаем аргументы, потом работаем с heap.
    let arr_idx = vm.heap.alloc_array(new_arr);

    // Записываем ссылку в регистр
    vm.current_frame_mut().registers[dst_reg] = ValueOrRef::new_array_ref(arr_idx as u64);
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
