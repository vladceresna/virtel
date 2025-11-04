use std::f64;

use bincode::{Decode, Encode};
use serde::{Deserialize, Serialize};

use crate::log::log;

#[derive(Debug, Copy, Clone, Encode, Decode, Serialize, Deserialize)]
enum ValueType {
    Byte,
    I64,
    F64,
    Bool,
    Str,
    Array,
    Ref,
    Func,
    Null,
}

#[derive(Debug, Copy, Clone, Encode, Decode, Serialize, Deserialize)]
struct Value {
    typ: ValueType,
    bits: u64,
}
impl Value {
    fn i64(v: i64) -> Self {
        Value {
            typ: ValueType::I64,
            bits: v as u64,
        }
    }
    fn as_i64(&self) -> i64 {
        self.bits as i64
    }
    fn f64(v: f64) -> Self {
        Value {
            typ: ValueType::F64,
            bits: f64::to_bits(v),
        }
    }
    fn as_f64(&self) -> f64 {
        f64::from_bits(self.bits)
    }
    fn bool(v: bool) -> Self {
        Value {
            typ: ValueType::Bool,
            bits: v as u64,
        }
    }
    fn as_bool(&self) -> bool {
        self.bits != 0
    }
    fn byte(v: u8) -> Self {
        Value {
            typ: ValueType::Byte,
            bits: v as u64,
        }
    }
    fn as_byte(&self) -> u8 {
        self.bits as u8
    }
    fn str_ref(idx: u64) -> Self {
        Value {
            typ: ValueType::Str,
            bits: idx,
        }
    }
    fn as_str_ref(&self) -> u64 {
        self.bits
    }
    fn array_ref(idx: u64) -> Self {
        Value {
            typ: ValueType::Array,
            bits: idx,
        }
    }
    fn as_array_ref(&self) -> u64 {
        self.bits
    }
    fn ref_(idx: u64) -> Self {
        Value {
            typ: ValueType::Ref,
            bits: idx,
        }
    }
    fn as_ref(&self) -> u64 {
        self.bits
    }
    fn func_ref(idx: u64) -> Self {
        Value {
            typ: ValueType::Func,
            bits: idx,
        }
    }
    fn as_func_ref(&self) -> u64 {
        self.bits
    }
    fn null() -> Self {
        Value {
            typ: ValueType::Null,
            bits: 0,
        }
    }
}

/// -------------------  Оpcodes  -------------------

const OP_LOAD_CONST: u8 = 1;
const OP_CAST_I64_TO_F64: u8 = 2;
const OP_ADD: u8 = 2;
const OP_SUB: u8 = 3;
const OP_MUL: u8 = 4;
const OP_DIV: u8 = 5;
const OP_EQ: u8 = 6;
const OP_SYSTEM_WRITE: u8 = 7;
const OP_CALL_NATIVE: u8 = 8;

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

fn op_add(vm: &mut VM) {
    let dst = vm.read_u8() as usize;
    let a = vm.registers[vm.read_u8() as usize];
    let b = vm.registers[vm.read_u8() as usize];
    vm.registers[dst] = Value {
        typ: ValueType::I64,
        bits: (a.bits + b.bits),
    };
}

fn op_div(vm: &mut VM) {
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
    table[OP_LOAD_CONST as usize] = op_load_const as OpHandler;
    table[OP_ADD as usize] = op_add as OpHandler;
    table[OP_SUB as usize] = op_sub as OpHandler;
    table[OP_MUL as usize] = op_mul as OpHandler;
    table[OP_DIV as usize] = op_div as OpHandler;
    table[OP_EQ as usize] = op_eq as OpHandler;
    table[OP_SYSTEM_WRITE as usize] = op_system_write as OpHandler;
    table[OP_CALL_NATIVE as usize] = op_call_native as OpHandler;
    table
}

static DISPATCH_TABLE: [OpHandler; 256] = build_dispatch_table();

/// -------------------  Chunk  -------------------
#[derive(Debug, Clone, Serialize, Deserialize, Encode, Decode)]
pub struct Chunk {
    pub code: Vec<u8>,
    pub constants: Vec<Value>,
    pub strings: Vec<String>,
}

impl Chunk {
    pub fn new() -> Self {
        Self {
            code: Vec::new(),
            constants: Vec::new(),
            strings: Vec::new(),
        }
    }

    pub fn emit(&mut self, op: u8, args: &[u8]) {
        self.code.push(op);
        self.code.extend_from_slice(args);
    }

    pub fn add_const(&mut self, v: Value) -> u16 {
        let idx = self.constants.len() as u16;
        self.constants.push(v);
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
    chunk: &'a Chunk,
    ip: usize,
    registers: [Value; 256],
    plugin_handlers: Vec<Option<OpHandler>>,
}

impl<'a> VM<'a> {
    pub fn new(chunk: &'a Chunk) -> Self {
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
        while self.ip < self.chunk.code.len() {
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
        let b = self.chunk.code[self.ip];
        self.ip += 1;
        b
    }

    #[inline(always)]
    fn read_u16(&mut self) -> u16 {
        let hi = self.chunk.code[self.ip] as u16;
        let lo = self.chunk.code[self.ip + 1] as u16;
        self.ip += 2;
        (hi << 8) | lo
    }
}
