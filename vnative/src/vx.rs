use std::vec;

use bincode::{Decode, Encode};
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Encode, Decode, PartialEq, Serialize, Deserialize)]
pub enum Value {
    Number(i64),
    Double(f64),
    String(String),
}
impl Value {
    fn as_i64(&self) -> i64 {
        match self {
            Value::Number(n) => *n,
            Value::Double(_) => panic!("Type error: Cannot convert Double to i64"),
            Value::String(_) => panic!("Type error: Cannot convert String to i64"),
        }
    }
}

pub type Reg = u16;

#[derive(Debug, Clone, Copy, Encode, Decode, PartialEq, Deserialize, Serialize)]
pub enum Instruction {
    LoadConstant { dst: Reg, const_id: u16 },
    Add { dst: Reg, lhs: Reg, rhs: Reg },
    Mul { dst: Reg, lhs: Reg, rhs: Reg },
    Return { src: Reg },
    Call { function: Reg },
    RunApp { name: Reg },
    CreateWindow { title: Reg, width: Reg, height: Reg },
}

#[derive(Encode, Decode, Serialize, Deserialize)]
pub struct Chunk {
    pub instructions: Vec<Instruction>,
    pub constants: Vec<Value>,
}

pub struct VM<'a> {
    chunk: &'a Chunk,
    ip: usize,
    registers: Vec<Value>,
}

impl<'a> VM<'a> {
    pub fn new(chunk: &'a Chunk) -> Self {
        Self {
            chunk, // Just save transferred link
            ip: 0,
            registers: vec![Value::Number(0); 256],
        }
    }

    pub fn run(&mut self) -> i64 {
        loop {
            let instruction = self.chunk.instructions[self.ip];
            self.ip += 1;

            match instruction {
                Instruction::LoadConstant { dst, const_id } => {
                    self.registers[dst as usize] = self.chunk.constants[const_id as usize].clone();
                }
                Instruction::Add { dst, lhs, rhs } => {
                    if let (Value::Number(l), Value::Number(r)) = (
                        self.registers[lhs as usize].clone(),
                        self.registers[rhs as usize].clone(),
                    ) {
                        self.registers[dst as usize] = Value::Number(l + r);
                    } else {
                        panic!("Type error in Add instruction");
                    }
                }
                Instruction::Mul { dst, lhs, rhs } => {
                    if let (Value::Number(l), Value::Number(r)) = (
                        self.registers[lhs as usize].clone(),
                        self.registers[rhs as usize].clone(),
                    ) {
                        self.registers[dst as usize] = Value::Number(l * r);
                    } else {
                        panic!("Type error in Mul instruction");
                    }
                }
                Instruction::Return { src } => {
                    return self.registers[src as usize].as_i64();
                }
                Instruction::Call { function } => {}
                Instruction::CreateWindow {
                    title,
                    width,
                    height,
                } => {}
                Instruction::RunApp { name } => {}
            }
        }
    }
}

//tests
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_vm_simple_return() {
        let program = Chunk {
            constants: vec![
                Value::Number(42), // Constant 42 locates at index 0
            ],
            instructions: vec![
                // 2. Using right format of instruction: load const with ID=0.
                Instruction::LoadConstant {
                    dst: 0,
                    const_id: 0,
                },
                Instruction::Return { src: 0 },
            ],
        };

        // 3. Transfer in VM link on our Chunk.
        let mut vm = VM::new(&program);
        let result = vm.run();
        assert_eq!(result, 42);
    }

    #[test]
    fn test_vm_arithmetic() {
        // Programm: (10 + 20) * 2
        // r0 = 10
        // r1 = 20
        // r2 = r0 + r1
        // r3 = 2
        // r4 = r2 * r3
        // return r4
        let program = Chunk {
            constants: vec![Value::Number(10), Value::Number(20), Value::Number(2)],
            instructions: vec![
                Instruction::LoadConstant {
                    dst: 0,
                    const_id: 0,
                }, // r0 = 10
                Instruction::LoadConstant {
                    dst: 1,
                    const_id: 1,
                }, // r1 = 20
                Instruction::Add {
                    dst: 2,
                    lhs: 0,
                    rhs: 1,
                }, // r2 = r0 + r1
                Instruction::LoadConstant {
                    dst: 3,
                    const_id: 2,
                }, // r3 = 2
                Instruction::Mul {
                    dst: 4,
                    lhs: 2,
                    rhs: 3,
                }, // r4 = r2 * r3
                Instruction::Return { src: 4 },
            ],
        };

        let mut vm = VM::new(&program);
        let result = vm.run();
        assert_eq!(result, 60);
    }
}
