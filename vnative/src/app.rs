use std::{
    fs,
    sync::{Arc, Mutex},
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

/// Apps structure
/// App:
/// - global app data
/// - threads
/// - main App object in bytecode
/// - smth else
/// One heap by app:
/// - full objects which referenced by fields
/// Object:
/// - fields (variables, functions, objects references etc.)
/// App:
/// - Heap
/// - Main Object (Which also have main function)
/// Main Object:
/// - it is just nested objects and functions
/// Example:
/// - app
///     - heap
///         - hello_string
///         - name_string
///         - main_function
///         - friends_array (single-typed static vector)
///             - hello_string
///             - name_string
///         - main_object
///             - main_function_ref
///             - hello_string_ref
///             - 47_number_as_is
///             - 3.7_float_as_is
///             - user_object_ref
///         - user_object (multi-typed dynamic vector)
///             - friends_array_ref
///             - name_string_ref
///             - age_number_as_is
///         - main_function
///     - main_object_ref (not written, because always is 0)
///
///
/// Objects is arrays
///

pub struct AppHeap {
    pub strings: Arc<Mutex<Vec<String>>>,
    pub arrays: Arc<Mutex<Vec<Vec<ValueOrRef>>>>,
    pub functions: Arc<Mutex<Vec<Function>>>,
}
impl AppHeap {
    pub fn new() -> Self {
        Self {
            strings: Arc::new(Mutex::new(Vec::new())),
            arrays: Arc::new(Mutex::new(Vec::new())),
            functions: Arc::new(Mutex::new(Vec::new())),
        }
    }
    /// Добавляет строку в кучу и возвращает её индекс (указатель)
    pub fn alloc_string(&self, s: String) -> usize {
        let mut strings = self.strings.lock().unwrap();
        let index = strings.len();
        strings.push(s);
        index
    }
    /// Получает копию строки по индексу
    pub fn get_string(&self, index: usize) -> Option<String> {
        let strings = self.strings.lock().unwrap();
        strings.get(index).cloned()
    }
    // --- Работа с массивами ---
    /// Добавляет массив в кучу и возвращает его индекс
    pub fn alloc_array(&self, arr: Vec<ValueOrRef>) -> usize {
        let mut arrays = self.arrays.lock().unwrap();
        let index = arrays.len();
        arrays.push(arr);
        index
    }
    /// Получает копию массива по индексу
    pub fn get_array(&self, index: usize) -> Option<Vec<ValueOrRef>> {
        let arrays = self.arrays.lock().unwrap();
        arrays.get(index).cloned()
    }
    /// Обновляет элемент внутри массива, который лежит в куче
    pub fn array_set_at(
        &self,
        array_idx: usize,
        item_idx: usize,
        value: ValueOrRef,
    ) -> Result<(), String> {
        let mut arrays = self.arrays.lock().unwrap();
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
    // --- Работа с функциями ---
    /// Регистрирует функцию и возвращает её индекс
    pub fn alloc_function(&self, func: Function) -> usize {
        let mut functions = self.functions.lock().unwrap();
        let index = functions.len();
        functions.push(func);
        index
    }
    pub fn get_function(&self, index: usize) -> Option<Function> {
        let functions = self.functions.lock().unwrap();
        functions.get(index).cloned()
    }
}

#[derive(Debug, Clone, Encode, Decode, Serialize, Deserialize)]
pub struct AppHeapSerialized {
    pub strings: Vec<String>,
    pub arrays: Vec<Vec<ValueOrRef>>,
    pub functions: Vec<Function>,
}

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
    /// metadata
    id: String,
    name: String,
    version: String,
    icon_path: String,
    /// status
    status: AppStatus,
    permissions: Permissions,
    /// threads
    threads: Vec<JoinHandle<()>>,
    /// main app object
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
            format!("App crated: {}", app_id).as_str(),
        );
        self.data.lock().unwrap().status = AppStatus::Running;
        // Starting VM
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
    // pub fn run_new_thread(future: F) -> Result<(), Error> {
    //     let handle = get_tokio().spawn(future);
    //     self.data.lock().unwrap().threads.push(handle);
    // }
}

#[derive(Debug, Copy, PartialEq, Clone, Encode, Decode, Serialize, Deserialize)]
pub enum ValueOrRefType {
    /// 8-bit unsigned integer (0 … 255). - Stored directly in `Value.bits`.
    Byte,
    /// 64-bit signed integer (-2⁶³ … 2⁶³-1). - Stored directly in `Value.bits` as raw `i64` representation.
    I64,
    /// 64-bit floating-point number (IEEE-754 double). - Stored in `Value.bits` as raw bits via `f64::to_bits()`.
    F64,
    /// `bits = 0` → `false`, `bits = 1` → `true`. - Any non-zero value is treated as `true`.
    Bool,
    /// `bits` holds the **index** (`u16`) into the string table.
    StrRef,
    /// `bits` holds the **index** (`u16`) into the array table.
    ArrayRef,
    /// `bits` holds the **index** (`u16`) into the function table.
    FuncRef,
    /// `bits` is always `0`.
    Null,
}

#[derive(Debug, Copy, PartialEq, Clone, Encode, Decode, Serialize, Deserialize)]
pub struct ValueOrRef {
    typ: ValueOrRefType,
    bits: u64,
}
impl ValueOrRef {
    fn type_is(&self, compared_typ: ValueOrRefType) -> bool {
        self.typ == compared_typ
    }
    fn new_i64(v: i64) -> Self {
        ValueOrRef {
            typ: ValueOrRefType::I64,
            bits: v as u64,
        }
    }
    fn as_i64(&self) -> i64 {
        self.bits as i64
    }
    fn new_f64(v: f64) -> Self {
        ValueOrRef {
            typ: ValueOrRefType::F64,
            bits: f64::to_bits(v),
        }
    }
    fn as_f64(&self) -> f64 {
        f64::from_bits(self.bits)
    }
    fn new_bool(v: bool) -> Self {
        ValueOrRef {
            typ: ValueOrRefType::Bool,
            bits: v as u64,
        }
    }
    fn as_bool(&self) -> bool {
        self.bits != 0
    }
    fn new_byte(v: u8) -> Self {
        ValueOrRef {
            typ: ValueOrRefType::Byte,
            bits: v as u64,
        }
    }
    fn as_byte(&self) -> u8 {
        self.bits as u8
    }
    fn new_str_ref(idx: u64) -> Self {
        ValueOrRef {
            typ: ValueOrRefType::StrRef,
            bits: idx,
        }
    }
    fn as_str_ref(&self) -> u64 {
        self.bits
    }
    fn new_array_ref(idx: u64) -> Self {
        ValueOrRef {
            typ: ValueOrRefType::ArrayRef,
            bits: idx,
        }
    }
    fn as_array_ref(&self) -> u64 {
        self.bits
    }
    fn new_func_ref(idx: u64) -> Self {
        ValueOrRef {
            typ: ValueOrRefType::FuncRef,
            bits: idx,
        }
    }
    fn as_func_ref(&self) -> u64 {
        self.bits
    }
    fn new_null() -> Self {
        ValueOrRef {
            typ: ValueOrRefType::Null,
            bits: 0,
        }
    }
}

enum Op {
    LoadConst,
    Move,
    Add,
    Subtract,
    Multiply,
    Divide,
    LowerThan,
    GreaterThan,
    Eq,
    Or,
    Not,
    Goto,
    GotoIf,
    NewFunction,
    CallFunction,
    Return,
    NewArray,
    GetArrayItem,
    SetArrayItem,
    GetArrayLength,
    IsType,
    SystemWrite,
    CallNative,
}

pub type OpHandler = fn(&mut Flow);

fn invalid_op(_flow: &mut Flow) {
    panic!("invalid opcode");
}
fn op_load_const(flow: &mut Flow) {
    let frame = flow.current_frame_mut();

    let dst = frame.read_u8() as usize;
    let cid = frame.read_u16() as usize;

    let val = frame.function.worktable.constants[cid];
    frame.registers[dst] = val;
}

fn op_move(flow: &mut Flow) {
    let dst = flow.read_u8() as usize;
    let cid = flow.read_u16() as usize;
    flow.registers[dst] = flow.function.worktable.constants[cid];
}
fn op_add(flow: &mut Flow) {
    let frame = flow.current_frame_mut();

    let dst = frame.read_u8() as usize;
    let idx_a = frame.read_u8() as usize;
    let idx_b = frame.read_u8() as usize;

    let a = frame.registers[idx_a].as_i64();
    let b = frame.registers[idx_b].as_i64();

    frame.registers[dst] = ValueOrRef::new_i64(a + b);
}
fn op_subtract(flow: &mut Flow) {
    let dst = vm.read_u8() as usize;
    let a = vm.registers[vm.read_u8() as usize].as_i64();
    let b = vm.registers[vm.read_u8() as usize].as_i64();
    vm.registers[dst] = ValueOrRef {
        typ: ValueOrRefType::I64,
        bits: (a - b) as u64,
    };
}
fn op_multiply(flow: &mut Flow) {
    let dst = vm.read_u8() as usize;
    let a = vm.registers[vm.read_u8() as usize];
    let b = vm.registers[vm.read_u8() as usize];
    vm.registers[dst] = ValueOrRef {
        typ: ValueOrRefType::I64,
        bits: a.bits * b.bits,
    };
}
fn op_divide(flow: &mut Flow) {
    let dst = vm.read_u8() as usize;
    let a = vm.registers[vm.read_u8() as usize];
    let b = vm.registers[vm.read_u8() as usize];
    vm.registers[dst] = ValueOrRef {
        typ: ValueOrRefType::I64,
        bits: a.bits / b.bits,
    };
}
fn op_eq(flow: &mut Flow) {
    let dst = vm.read_u8() as usize;
    let a = vm.registers[vm.read_u8() as usize];
    let b = vm.registers[vm.read_u8() as usize];
    let eq = a.bits == b.bits;
    vm.registers[dst] = ValueOrRef {
        typ: ValueOrRefType::Bool,
        bits: eq as u64,
    };
}

fn op_call(flow: &mut Flow) {
    let (func_idx, dest_reg) = {
        let frame = flow.current_frame_mut();
        (frame.read_u16() as usize, frame.read_u8() as usize)
    };
    if let Some(func) = flow.heap.get_function(func_idx) {
        let new_frame = FunctionFunnel::new(Arc::new(func), dest_reg);
        flow.functions_stack.push(new_frame);
    } else {
        panic!("Function not found id: {}", func_idx);
    }
}

fn op_return(flow: &mut Flow) {
    let (ret_val, dest_reg) = {
        let frame = flow.current_frame_mut();
        let src = frame.read_u8() as usize;
        (frame.registers[src], frame.return_to_reg)
    };
    flow.functions_stack.pop();
    if let Some(parent_frame) = flow.functions_stack.last_mut() {
        parent_frame.registers[dest_reg] = ret_val;
    }
}

fn op_call_native(flow: &mut Flow) {
    let _plugin_id = flow.read_u8();
    let _fun_id = flow.read_u8();
    let _args_reg = flow.read_u8() as usize;
    println!("CALL_NATIVE: not implemented");
}

const fn build_dispatch_table() -> [OpHandler; 256] {
    let mut table = [invalid_op as OpHandler; 256];
    table[Op::LoadConst as usize] = op_load_const as OpHandler;
    table[Op::Move as usize] = op_move as OpHandler;
    table[Op::Add as usize] = op_add as OpHandler;
    table[Op::Subtract as usize] = op_subtract as OpHandler;
    table[Op::Multiply as usize] = op_multiply as OpHandler;
    table[Op::Divide as usize] = op_divide as OpHandler;
    table[Op::NewFunction as usize] = op_eq as OpHandler;
    table[Op::CallFunction as usize] = op_eq as OpHandler;
    table[Op::Return as usize] = op_eq as OpHandler;
    table[Op::CallNative as usize] = op_call_native as OpHandler;
    table
}

static DISPATCH_TABLE: [OpHandler; 256] = build_dispatch_table();

pub struct Flow {
    functions_stack: Vec<FunctionFunnel>,
    heap: Arc<AppHeap>,
}
impl Flow {
    pub fn new(heap: Arc<AppHeap>) -> Self {
        // Создаем первый фрейм (Main)
        let main_func = heap.get_function(0).unwrap();
        let main_frame = FunctionFunnel::new(main_func, 0);

        Self {
            functions_stack: vec![main_frame],
            heap,
        }
    }
    pub fn run(&mut self) {
        // Главный цикл процессора
        loop {
            // 1. Если фреймов нет — программа завершена
            if self.frames.is_empty() {
                break;
            }

            // 2. Берем АКТИВНЫЙ фрейм (верхний в стопке)
            // Мы не можем взять &mut self целиком, поэтому используем трюк
            // Обычно логику выносят, но для примера упростим:

            let frame_idx = self.frames.len() - 1;
            let frame = &mut self.frames[frame_idx];

            // 3. Проверяем, не закончилась ли функция
            if frame.ip >= frame.function.instructions.len() {
                // Функция кончилась без явного return -> просто убираем её
                self.functions_stack.pop();
                continue;
            }

            // 4. Читаем опкод
            let op = frame.read_u8();
            //a
            while self.ip < frame.function.instructions.len() {
                let op = self.read_u8() as usize;
                let handler = DISPATCH_TABLE[op];
                handler(self);
            }
            //b
            // 5. Диспетчеризация
            match op {
                // ... арифметика работает с frame.registers ...
                // === САМОЕ ИНТЕРЕСНОЕ: ВЫЗОВ ФУНКЦИИ ===
                OP_CALL_FUNCTION => {
                    // Допустим формат: [OP] [FuncIndex: u16] [DestReg: u8]
                    let func_idx = frame.read_u16() as usize;
                    let dest_reg = frame.read_u8() as usize;
                    // 1. Находим функцию в куче
                    // (тут нужна логика получения функции из heap или worktable)
                    let func_ref = &some_global_functions[func_idx];
                    // 2. Создаем НОВЫЙ фрейм
                    let new_frame = CallFrame::new(func_ref, dest_reg);
                    // 3. ⚠️ Передача аргументов!
                    // Обычно мы копируем значения из регистров ТЕКУЩЕГО фрейма
                    // в регистры НОВОГО фрейма (R0, R1...)
                    // 4. Добавляем фрейм в стек VM
                    // В следующей итерации loop мы будем исполнять уже ЕГО!
                    self.frames.push(new_frame);
                }
                // === ВОЗВРАТ ИЗ ФУНКЦИИ ===
                OP_RETURN => {
                    // Допустим формат: [OP] [SrcReg: u8]
                    let src_reg = frame.read_u8() as usize;
                    let return_value = frame.registers[src_reg]; // Берем результат
                    let dest_reg = frame.return_to_reg; // Куда его положить?
                                                        // Удаляем текущий фрейм (он завершен)
                    self.frames.pop();
                    // Если есть куда возвращаться — записываем результат
                    if let Some(parent_frame) = self.frames.last_mut() {
                        parent_frame.registers[dest_reg] = return_value;
                    }
                }

                _ => { /* обработка остальных опкодов */ }
            }
        }
    }

    #[inline(always)]
    pub fn current_frame_mut(&mut self) -> &mut FunctionFunnel {
        let last = self.functions_stack.len() - 1;
        &mut self.functions_stack[last]
    }
}

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

pub struct FunctionFunnel {
    pub function: Arc<Function>,
    pub ip: usize,
    pub registers: [ValueOrRef; 256],
    pub return_to_reg: usize,
}
impl FunctionFunnel {
    pub fn new(func: Arc<Function>, return_to_reg: usize) -> Self {
        Self {
            function: func,
            ip: 0,
            // Инициализируем нулями или Null
            registers: [ValueOrRef::new_null(); 256],
            return_to_reg,
        }
    }
    #[inline(always)]
    pub fn read_u8(&mut self) -> u8 {
        let b = self.function.instructions[self.ip];
        self.ip += 1;
        b
    }
    #[inline(always)]
    pub fn read_u16(&mut self) -> u16 {
        let hi = self.function.instructions[self.ip] as u16;
        let lo = self.function.instructions[self.ip + 1] as u16;
        self.ip += 2;
        (hi << 8) | lo
    }
}
