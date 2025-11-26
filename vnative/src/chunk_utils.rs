use bincode::{config, decode_from_slice, encode_to_vec};

use crate::app_file::AppFile;

/// VX Bytecode App Structure = Heap
/// Encoded via bincode = .VC
/// Encoded via JSON = .VS
/// Low-level programming language = Steps
/// High-level programming language = Stick
/// Human-level programming language = Wit

pub fn vs_to_vc(vs: String) -> Vec<u8> {
    return heap_to_vc(&vs_to_heap(vs));
}
pub fn vc_to_vs(vc: Vec<u8>) -> String {
    return heap_to_vs(&vc_to_heap(vc));
}

pub fn vs_to_heap(vs: String) -> AppFile {
    return serde_json::from_str(vs.as_str()).unwrap();
}
pub fn heap_to_vs(app_file: &AppFile) -> String {
    return serde_json::to_string(app_file).unwrap();
}

pub fn vc_to_heap(vc: Vec<u8>) -> AppFile {
    let config = config::standard();
    let (decoded, _len): (AppFile, usize) =
        decode_from_slice(&vc[..], config).expect("Failed to decode Chunk");
    return decoded;
}

pub fn heap_to_vc(app_file: &AppFile) -> Vec<u8> {
    let config = config::standard();
    let encoded = encode_to_vec(app_file, config).expect("Failed to encode Chunk");
    return encoded;
}
