use std::f64;

use bincode::{Decode, Encode};
use serde::{Deserialize, Serialize};

#[derive(Debug, Copy, PartialEq, Clone, Encode, Decode, Serialize, Deserialize)]
enum ValueType {
    /// 8-bit unsigned integer (0 … 255).
    /// Stored directly in `Value.bits`.
    Byte,
    /// 64-bit signed integer (-2⁶³ … 2⁶³-1).
    /// Stored directly in `Value.bits` as raw `i64` representation.
    I64,
    /// 64-bit floating-point number (IEEE-754 double).
    /// Stored in `Value.bits` as raw bits via `f64::to_bits()`.
    F64,
    /// Boolean value.
    /// `bits = 0` → `false`, `bits = 1` → `true`.
    /// Any non-zero value is treated as `true`.
    Bool,
    /// Reference to a string in `Chunk.strings`.
    /// `bits` holds the **index** (`u16`) into the string table.
    /// The actual string is stored in `Chunk.strings[idx]`.
    Str,
    /// Reference to an array in `Chunk.arrays`.
    /// `bits` holds the **index** (`u16`) into the array table.
    /// An array is a `Vec<Value>` and can hold mixed types.
    Array,
    /// Reference to an object in the VM heap (`VM.heap`).
    /// `bits` holds the **index** (`u32`) into the dynamic heap.
    /// Used for large objects, BigInt, custom structs, etc.
    Ref,
    /// Reference to a function/closure in `Chunk.funcs`.
    /// `bits` holds the **index** (`u16`) into the function table.
    /// A function contains: code offset, arity, optional name.
    Func,
    /// Null value (equivalent to `null`/`nil`).
    /// `bits` is always `0`.
    /// Represents the absence of a value.
    Null,
}

#[derive(Debug, Copy, PartialEq, Clone, Encode, Decode, Serialize, Deserialize)]
struct Value {
    typ: ValueType,
    bits: u64,
}
impl Value {
    fn type_is(&self, compared_typ: ValueType) -> bool {
        self.typ == compared_typ
    }
    fn new_i64(v: i64) -> Self {
        Value {
            typ: ValueType::I64,
            bits: v as u64,
        }
    }
    fn as_i64(&self) -> i64 {
        self.bits as i64
    }
    fn new_f64(v: f64) -> Self {
        Value {
            typ: ValueType::F64,
            bits: f64::to_bits(v),
        }
    }
    fn as_f64(&self) -> f64 {
        f64::from_bits(self.bits)
    }
    fn new_bool(v: bool) -> Self {
        Value {
            typ: ValueType::Bool,
            bits: v as u64,
        }
    }
    fn as_bool(&self) -> bool {
        self.bits != 0
    }
    fn new_byte(v: u8) -> Self {
        Value {
            typ: ValueType::Byte,
            bits: v as u64,
        }
    }
    fn as_byte(&self) -> u8 {
        self.bits as u8
    }
    fn new_str_ref(idx: u64) -> Self {
        Value {
            typ: ValueType::Str,
            bits: idx,
        }
    }
    fn as_str_ref(&self) -> u64 {
        self.bits
    }
    fn new_array_ref(idx: u64) -> Self {
        Value {
            typ: ValueType::Array,
            bits: idx,
        }
    }
    fn as_array_ref(&self) -> u64 {
        self.bits
    }
    fn new_ref_(idx: u64) -> Self {
        Value {
            typ: ValueType::Ref,
            bits: idx,
        }
    }
    fn as_ref(&self) -> u64 {
        self.bits
    }
    fn new_func_ref(idx: u64) -> Self {
        Value {
            typ: ValueType::Func,
            bits: idx,
        }
    }
    fn as_func_ref(&self) -> u64 {
        self.bits
    }
    fn new_null() -> Self {
        Value {
            typ: ValueType::Null,
            bits: 0,
        }
    }
}

/// -------------------  Оpcodes  -------------------

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

/// -------------------  Handlers  -------------------
type OpHandler = fn(&mut VM);

fn invalid_op(_vm: &mut VM) {
    panic!("invalid opcode");
}

fn op_load_const(vm: &mut VM) {
    let dst = vm.read_u8() as usize;
    let cid = vm.read_u16() as usize;
    vm.registers[dst] = vm.chunk.constants[cid];
}
fn op_move(vm: &mut VM) {
    let dst = vm.read_u8() as usize;
    let cid = vm.read_u16() as usize;
    vm.registers[dst] = vm.chunk.constants[cid];
}

fn op_add(vm: &mut VM) {
    let dst = vm.read_u8() as usize;
    let a = vm.registers[vm.read_u8() as usize].as_i64();
    let b = vm.registers[vm.read_u8() as usize].as_i64();
    vm.registers[dst] = Value {
        typ: ValueType::I64,
        bits: (a + b) as u64,
    };
}
fn op_subtract(vm: &mut VM) {
    let dst = vm.read_u8() as usize;
    let a = vm.registers[vm.read_u8() as usize].as_i64();
    let b = vm.registers[vm.read_u8() as usize].as_i64();
    vm.registers[dst] = Value {
        typ: ValueType::I64,
        bits: (a - b) as u64,
    };
}

fn op_multiply(vm: &mut VM) {
    let dst = vm.read_u8() as usize;
    let a = vm.registers[vm.read_u8() as usize];
    let b = vm.registers[vm.read_u8() as usize];
    vm.registers[dst] = Value {
        typ: ValueType::I64,
        bits: a.bits * b.bits,
    };
}
fn op_divide(vm: &mut VM) {
    let dst = vm.read_u8() as usize;
    let a = vm.registers[vm.read_u8() as usize];
    let b = vm.registers[vm.read_u8() as usize];
    vm.registers[dst] = Value {
        typ: ValueType::I64,
        bits: a.bits / b.bits,
    };
}

fn op_eq(vm: &mut VM) {
    let dst = vm.read_u8() as usize;
    let a = vm.registers[vm.read_u8() as usize];
    let b = vm.registers[vm.read_u8() as usize];
    let eq = a.bits == b.bits;
    vm.registers[dst] = Value {
        typ: ValueType::Bool,
        bits: eq as u64,
    };
}

fn op_call_native(vm: &mut VM) {
    let _plugin_id = vm.read_u8();
    let _fun_id = vm.read_u8();
    let _args_reg = vm.read_u8() as usize;
    println!("CALL_NATIVE: not implemented");
}

/// -------------------  Dispatch Table  -------------------
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

/// -------------------  Function  -------------------

/// function [id](var: type, var: type) -> type {
///     code;
///     return value;
/// }

#[derive(Debug, Clone, Serialize, Deserialize, Encode, Decode)]
pub struct FunctionSignature {
    pub args: Vec<ValueType>,
    pub result_type: ValueType,
}
impl FunctionSignature {
    pub fn new(args: Vec<ValueType>, result_type: ValueType) -> FunctionSignature {
        Self { args, result_type }
    }
}
#[derive(Debug, Clone, Serialize, Deserialize, Encode, Decode)]
pub struct FunctionWorkTable {
    pub args: Vec<Value>,
    pub constants: Vec<Value>,
}
impl FunctionWorkTable {
    pub fn new(args: Vec<Value>, constants: Vec<Value>) -> FunctionWorkTable {
        FunctionWorkTable { args, constants }
    }
}
#[derive(Debug, Clone, Serialize, Deserialize, Encode, Decode)]
pub struct Object {
    pub fields: Vec<ValueType>,
}

#[derive(Debug, Clone, Serialize, Deserialize, Encode, Decode)]
pub struct Function {
    pub signature: FunctionSignature,
    pub worktable: FunctionWorkTable,
    pub instructions: Vec<u8>,
}

impl Function {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new(Vec::new(), ValueType::Bool),
            worktable: FunctionWorkTable::new(Vec::new(), Vec::new()),
            instructions: Vec::new(),
        }
    }

    pub fn emit(&mut self, op: u8, args: &[u8]) {
        self.instructions.push(op);
        self.instructions.extend_from_slice(args);
    }

    pub fn add_const(&mut self, v: Value) -> u16 {
        let idx = self.worktable.constants.len() as u16;
        self.worktable.constants.push(v);
        idx
    }

    pub fn add_string(&mut self, s: String) -> u16 {
        let idx = self.strings.len() as u16;
        self.strings.push(s);
        idx
    }
}

/// -------------------  VM  -------------------

pub struct VM<'a> {
    chunk: &'a Function,
    ip: usize,
    registers: [Value; 256],
    plugin_handlers: Vec<Option<OpHandler>>,
}

impl<'a> VM<'a> {
    pub fn new(chunk: &'a Function) -> Self {
        let mut plugin_handlers = vec![None; 256];
        for i in 0..=8 {
            plugin_handlers[i] = None;
        }
        Self {
            chunk,
            ip: 0,
            registers: [Value {
                typ: ValueType::Null,
                bits: 0,
            }; 256],
            plugin_handlers,
        }
    }

    pub fn run(&mut self) {
        while self.ip < self.chunk.instructions.len() {
            let op = self.read_u8() as usize;
            let handler = self.plugin_handlers[op].unwrap_or(DISPATCH_TABLE[op]);
            handler(self);
        }
    }

    pub fn register_plugin(&mut self, op: u8, handler: OpHandler) {
        if op <= 8 {
            panic!("opcodes 0–8 reserved for core");
        }
        self.plugin_handlers[op as usize] = Some(handler);
    }

    #[inline(always)]
    fn read_u8(&mut self) -> u8 {
        let b = self.chunk.instructions[self.ip];
        self.ip += 1;
        b
    }

    #[inline(always)]
    fn read_u16(&mut self) -> u16 {
        let hi = self.chunk.instructions[self.ip] as u16;
        let lo = self.chunk.instructions[self.ip + 1] as u16;
        self.ip += 2;
        (hi << 8) | lo
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_simple_functions() {
        let mut function = Function::new();

        let const_5 = function.add_const(Value::new_i64(5));
        let const_3 = function.add_const(Value::new_i64(3));

        function.emit(
            Op::LoadConst as u8,
            &[0u8, (const_5 >> 8) as u8, const_5 as u8],
        );
        function.emit(
            Op::LoadConst as u8,
            &[1u8, (const_3 >> 8) as u8, const_3 as u8],
        );
        function.emit(Op::NewFunction as u8, &[2u8, 0u8, 1u8]);
        function.emit(Op::Return as u8, &[2u8, 0u8, 1u8]);
        function.emit(Op::CallFunction as u8, &[2u8, 0u8, 1u8]);

        let mut vm = VM::new(&function);
        vm.run();

        assert_eq!(vm.registers[2].typ, ValueType::I64);
        assert_eq!(vm.registers[2].as_i64(), 8);
    }
    #[test]
    fn test_simple_addition() {
        let mut function = Function::new();

        let const_5 = function.add_const(Value::new_i64(5));
        let const_3 = function.add_const(Value::new_i64(3));

        function.emit(
            Op::LoadConst as u8,
            &[0u8, (const_5 >> 8) as u8, const_5 as u8],
        );
        function.emit(
            Op::LoadConst as u8,
            &[1u8, (const_3 >> 8) as u8, const_3 as u8],
        );
        function.emit(Op::Add as u8, &[2u8, 0u8, 1u8]);

        let mut vm = VM::new(&function);
        vm.run();

        // Check result: reg 2 must be equal to 8 (as i64)
        assert_eq!(vm.registers[2].typ, ValueType::I64);
        assert_eq!(vm.registers[2].as_i64(), 8);
    }
}
