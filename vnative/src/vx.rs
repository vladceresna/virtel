//! vnative/src/vx.rs
//! Virtel VM – 27‑байтный байткод, NaN‑tagging, Dispatch Table
//! Копируй, запускай, работает.

use bincode::{Decode, Encode};
use serde::{Deserialize, Serialize};

/// -------------------  NaN‑tagging  -------------------
type Value = u64;

const TAG_INT: u64 = 0x7ff8_0000_0000_0000;
const TAG_TRUE: u64 = 0x7ff8_0000_0000_0001;
const TAG_FALSE: u64 = 0x7ff8_0000_0000_0002;
const TAG_NULL: u64 = 0x7ff8_0000_0000_0003;
const TAG_STRING: u64 = 0x7ff8_0000_0000_0004;

#[inline(always)]
pub fn int(v: i64) -> Value {
    TAG_INT | ((v as u64) & 0x0000_ffff_ffff_ffff)
}

#[inline(always)]
pub fn bool(v: bool) -> Value {
    if v {
        TAG_TRUE
    } else {
        TAG_FALSE
    }
}

#[inline(always)]
pub fn float(v: f64) -> Value {
    v.to_bits()
}

#[inline(always)]
pub fn is_int(v: Value) -> bool {
    (v >> 48) == 0x7ff8
}

#[inline(always)]
pub fn as_int(v: Value) -> i64 {
    ((v & 0x0000_ffff_ffff_ffff) as i64) << 16 >> 16
}

#[inline(always)]
pub fn is_float(v: Value) -> bool {
    let exp = (v >> 52) & 0x7ff;
    exp != 0x7ff8 && exp != 0x7ff0
}

#[inline(always)]
pub fn as_float(v: Value) -> f64 {
    f64::from_bits(v)
}

#[inline(always)]
pub fn is_bool(v: Value) -> bool {
    v == TAG_TRUE || v == TAG_FALSE
}

#[inline(always)]
pub fn as_bool(v: Value) -> bool {
    v == TAG_TRUE
}

#[inline(always)]
pub fn is_string(v: Value) -> bool {
    (v >> 48) == 0x7ff8 && (v & 0xffff_0000_0000_0000) == TAG_STRING
}

#[inline(always)]
pub fn as_string_index(v: Value) -> usize {
    (v & 0x0000_ffff_ffff_ffff) as usize
}

/// -------------------  Оpcodes  -------------------
const OP_LOAD_CONST: u8 = 1;
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
    if is_int(a) && is_int(b) {
        let sum = (a & 0x0000_ffff_ffff_ffff) + (b & 0x0000_ffff_ffff_ffff);
        vm.registers[dst] = TAG_INT | sum;
    } else if is_float(a) && is_float(b) {
        vm.registers[dst] = float(as_float(a) + as_float(b));
    } else {
        panic!("ADD: type error");
    }
}

fn op_sub(vm: &mut VM) {
    let dst = vm.read_u8() as usize;
    let a = vm.registers[vm.read_u8() as usize];
    let b = vm.registers[vm.read_u8() as usize];
    if is_int(a) && is_int(b) {
        let diff = as_int(a) - as_int(b);
        vm.registers[dst] = int(diff);
    } else if is_float(a) && is_float(b) {
        vm.registers[dst] = float(as_float(a) - as_float(b));
    } else {
        panic!("SUB: type error");
    }
}

fn op_mul(vm: &mut VM) {
    let dst = vm.read_u8() as usize;
    let a = vm.registers[vm.read_u8() as usize];
    let b = vm.registers[vm.read_u8() as usize];
    if is_int(a) && is_int(b) {
        let prod = as_int(a) * as_int(b);
        vm.registers[dst] = int(prod);
    } else if is_float(a) && is_float(b) {
        vm.registers[dst] = float(as_float(a) * as_float(b));
    } else {
        panic!("MUL: type error");
    }
}

fn op_div(vm: &mut VM) {
    let dst = vm.read_u8() as usize;
    let a = vm.registers[vm.read_u8() as usize];
    let b = vm.registers[vm.read_u8() as usize];
    if is_int(a) && is_int(b) {
        let b_i = as_int(b);
        if b_i == 0 {
            panic!("div by zero");
        }
        vm.registers[dst] = int(as_int(a) / b_i);
    } else if is_float(a) && is_float(b) {
        vm.registers[dst] = float(as_float(a) / as_float(b));
    } else {
        panic!("DIV: type error");
    }
}

fn op_eq(vm: &mut VM) {
    let dst = vm.read_u8() as usize;
    let a = vm.registers[vm.read_u8() as usize];
    let b = vm.registers[vm.read_u8() as usize];
    let eq = if is_int(a) && is_int(b) {
        a == b
    } else if is_float(a) && is_float(b) {
        as_float(a) == as_float(b)
    } else if is_bool(a) && is_bool(b) {
        a == b
    } else {
        false
    };
    vm.registers[dst] = bool(eq);
}

fn op_system_write(vm: &mut VM) {
    let reg = vm.read_u8() as usize;
    let v = vm.registers[reg];
    if is_int(v) {
        println!("{}", as_int(v));
    } else if is_float(v) {
        println!("{}", as_float(v));
    } else if is_bool(v) {
        println!("{}", as_bool(v));
    } else if is_string(v) {
        let idx = as_string_index(v);
        println!("{}", vm.chunk.strings[idx]);
    } else {
        println!("? <unknown>");
    }
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
            registers: [TAG_NULL; 256],
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
