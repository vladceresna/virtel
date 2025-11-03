use bincode::{config, decode_from_slice, encode_to_vec};

use crate::vx::Chunk;

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
