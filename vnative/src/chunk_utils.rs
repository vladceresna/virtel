use std::sync::{Arc, Mutex, RwLock};

use bincode::{config, decode_from_slice, encode_to_vec};

use crate::app::{Heap, AppHeapObject};

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

pub fn vs_to_heap(vs: String) -> AppHeapObject {
    return serde_json::from_str(vs.as_str()).unwrap();
}
pub fn heap_to_vs(chunk: &AppHeapObject) -> String {
    return serde_json::to_string(chunk).unwrap();
}

pub fn vc_to_heap(vc: Vec<u8>) -> AppHeapObject {
    let config = config::standard();
    let (decoded, _len): (AppHeapObject, usize) =
        decode_from_slice(&vc[..], config).expect("Failed to decode Chunk");
    return decoded;
}

pub fn heap_to_vc(chunk: &AppHeapObject) -> Vec<u8> {
    let config = config::standard();
    let encoded = encode_to_vec(chunk, config).expect("Failed to encode Chunk");
    return encoded;
}

pub fn app_heap_object_deserialize(heap: AppHeapObject) -> Heap {
    Heap {
        strings: Arc::new(RwLock::new(Arena::new()))),
        arrays: Arc::new(RwLock::new(heap.arrays)),
        functions: Arc::new(RwLock::new(heap.functions)),
    }
}

pub fn app_heap_object_serialize(heap: Heap) -> AppHeapObject {
    AppHeapObject {
        strings: heap.strings.read().unwrap(),
        arrays: (),
        functions: ()
    }
}
