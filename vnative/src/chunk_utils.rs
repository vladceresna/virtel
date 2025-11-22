use std::sync::{Arc, Mutex};

use bincode::{config, decode_from_slice, encode_to_vec};

use crate::app::{AppHeap, AppHeapSerialized};

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

pub fn vs_to_heap(vs: String) -> AppHeapSerialized {
    return serde_json::from_str(vs.as_str()).unwrap();
}
pub fn heap_to_vs(chunk: &AppHeapSerialized) -> String {
    return serde_json::to_string(chunk).unwrap();
}

pub fn vc_to_heap(vc: Vec<u8>) -> AppHeapSerialized {
    let config = config::standard();
    let (decoded, _len): (AppHeapSerialized, usize) =
        decode_from_slice(&vc[..], config).expect("Failed to decode Chunk");
    return decoded;
}

pub fn heap_to_vc(chunk: &AppHeapSerialized) -> Vec<u8> {
    let config = config::standard();
    let encoded = encode_to_vec(chunk, config).expect("Failed to encode Chunk");
    return encoded;
}

pub fn heap_to_deserialized(heap: AppHeapSerialized) -> AppHeap {
    AppHeap {
        strings: Arc::new(Mutex::new(heap.strings)),
        arrays: Arc::new(Mutex::new(heap.arrays)),
        functions: Arc::new(Mutex::new(heap.functions)),
    }
}
