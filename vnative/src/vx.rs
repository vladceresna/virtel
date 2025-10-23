use std::vec;

#[derive(Debug, Clone, Copy)]
pub enum Value {
    Number(i64),
}
impl Value {
    fn as_i64(&self) -> i64 {
        match self {
            Value::Number(n) => *n,
        }
    }
}

pub type Reg = u16;

#[derive(Debug, Clone, Copy)]
pub enum Instruction {
    LoadConstant { dst: Reg, const_id: u16 },
    Add { dst: Reg, lhs: Reg, rhs: Reg },
    Mul { dst: Reg, lhs: Reg, rhs: Reg },
    Return { src: Reg },
    Call {},
}

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
            chunk, // Просто сохраняем переданную ссылку
            ip: 0,
            registers: vec![Value::Number(0); 256],
        }
    }

    fn run(&mut self) -> i64 {
        loop {
            let instruction = self.chunk.instructions[self.ip];
            self.ip += 1;

            match instruction {
                Instruction::LoadConstant { dst, const_id } => {
                    self.registers[dst as usize] = self.chunk.constants[const_id as usize];
                }
                Instruction::Add { dst, lhs, rhs } => {
                    if let (Value::Number(l), Value::Number(r)) =
                        (self.registers[lhs as usize], self.registers[rhs as usize])
                    {
                        self.registers[dst as usize] = Value::Number(l + r);
                    } else {
                        panic!("Type error in Add instruction");
                    }
                }
                Instruction::Mul { dst, lhs, rhs } => {
                    if let (Value::Number(l), Value::Number(r)) =
                        (self.registers[lhs as usize], self.registers[rhs as usize])
                    {
                        self.registers[dst as usize] = Value::Number(l * r);
                    } else {
                        panic!("Type error in Mul instruction");
                    }
                }
                Instruction::Return { src } => {
                    return self.registers[src as usize].as_i64();
                }
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
                Value::Number(42), // Константа 42 находится по индексу 0
            ],
            instructions: vec![
                // 2. Используем правильный формат инструкции: загрузить константу с ID=0.
                Instruction::LoadConstant {
                    dst: 0,
                    const_id: 0,
                },
                Instruction::Return { src: 0 },
            ],
        };

        // 3. Передаем в ВМ ссылку на наш Chunk.
        let mut vm = VM::new(&program);
        let result = vm.run();
        assert_eq!(result, 42);
    }

    #[test]
    fn test_vm_arithmetic() {
        // Программа: (10 + 20) * 2
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
