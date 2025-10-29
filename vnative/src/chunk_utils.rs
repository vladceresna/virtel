use std::fs;

use bincode::{config, decode_from_slice, encode_to_vec};

use crate::vx::{Chunk, Instruction, Value};

/// VX Bytecode = Chunk
/// Encoded via bincode = .VC
/// Encoded via JSON = .VS
/// Low-level programming language = Steps
/// High-level programming language = Stick
/// Human-level programming language = Wit

pub fn vs_to_vc(vs: String) -> Vec<u8> {
    return chunk_to_vc(&vs_to_chunk(vs));
}
pub fn vc_to_vs(vc: Vec<u8>) -> String {
    return chunk_to_vs(&vc_to_chunk(vc));
}

pub fn vs_to_chunk(vs: String) -> Chunk {
    return serde_json::from_str(vs.as_str()).unwrap();
}
pub fn chunk_to_vs(chunk: &Chunk) -> String {
    return serde_json::to_string(chunk).unwrap();
}

pub fn vc_to_chunk(vc: Vec<u8>) -> Chunk {
    let config = config::standard();
    let (decoded, len): (Chunk, usize) =
        decode_from_slice(&vc[..], config).expect("Failed to encode Chunk");
    return decoded;
}

pub fn chunk_to_vc(chunk: &Chunk) -> Vec<u8> {
    let config = config::standard();
    let encoded = encode_to_vec(chunk, config).expect("Failed to encode Chunk");
    return encoded;
}

//tests
#[cfg(test)]
mod tests {
    use serde_json::json;

    use crate::center::get_virtel_center;

    use super::*;

    #[test]
    fn it_works() {
        let apps_dir = "/home/vladceresna/.virtel/0/sys/apps";
        let app_id = "vladceresna.virtel.launcher";
        let this_app_vc_path = format!("{}/{}/code/{}.vc", apps_dir, app_id, app_id);
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
        let vs = chunk_to_vs(&program);
        println!("{}", &vs);
        let vc = chunk_to_vc(&program);
        fs::write(this_app_vc_path, vc).unwrap();
        //let vs = json!({ "instructions":[""] }).to_string();
        assert_eq!(vc_to_vs(vs_to_vc(vs.clone())), vs);
    }
}
