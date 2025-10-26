use std::fs;

use bincode::{config, encode_to_vec};

use crate::vx::{Chunk, Instruction, Value};


/// .VC - Virtel Code (Encoded Bytecode)
///
pub fn save_steps_to

pub fn compile_to_chunk(input: &str) -> Chunk {
    let mut instructions = Vec::new();
    let mut constants = Vec::new();

    for line in input.lines() {
        let parts: Vec<&str> = line.split_whitespace().collect();
        match parts[0] {
            "load_const" => {
                let dst = parts[1].parse::<u16>().unwrap();
                let value = parts[2].parse::<i64>().unwrap();
                let const_id = constants.len() as u16;
                constants.push(Value::Number(value));
                instructions.push(Instruction::LoadConstant { dst, const_id });
            }
            "add" => {
                let dst = parts[1].parse::<u16>().unwrap();
                let lhs = parts[2].parse::<u16>().unwrap();
                let rhs = parts[3].parse::<u16>().unwrap();
                instructions.push(Instruction::Add { dst, lhs, rhs });
            }
            // Добавь другие инструкции (Mul, Return, etc.)
            _ => panic!("Unknown instruction: {}", parts[0]),
        }
    }

    Chunk {
        instructions,
        constants,
    }
}

pub fn save_chunk_to_vc(chunk: &Chunk, path: &str) {
    let config = config::standard();
    let encoded = encode_to_vec(chunk, config).expect("Failed to encode Chunk");
    fs::write(path, encoded).expect("Failed to write .vc file");
}
