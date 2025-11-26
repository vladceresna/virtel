use bincode::{config, Decode, Encode};
use serde::{Deserialize, Serialize};
use slotmap::{Key, SlotMap};

use crate::data::Constant;

#[derive(Debug, Serialize, Deserialize, Encode, Decode)]
pub struct AppFile {
    magic: [u8; 4],                                    // "VCLL"
    version: u8,                                       // 4...5...6...7 as Virtel versions
    entry_points: Vec<Box<Key>>, // it is only directive to system, but user can select another function to entry in
    global_data_initial_state: SlotMap<Key, Constant>, // data, that exists on start of instructions execution
}
impl AppFile {
    pub fn from_bytes(bytes: Vec<u8>) -> Self {
        let config = config::standard();
        let (app_file, _): (AppFile, _) = bincode::decode_from_slice(&bytes[..], config).unwrap();
        app_file
    }
    pub fn to_bytes(&self) -> Vec<u8> {
        let config = config::standard();
        let bytes: Vec<u8> = bincode::encode_to_vec(self, config).unwrap();
        bytes
    }
    pub fn get_global_data_initial_state(&self) -> SlotMap<Key, Constant> {
        self.global_data_initial_state // todo or .clone() or &self
    }
    pub fn get_entry_point_key(&self, index: usize) -> Key {
        *self
            .entry_points
            .get(index)
            .expect("Entry point index out of bounds")
    }
}
// ------ installer ------
// .lpp - as zip or apk
// .json - as manifest
// .vxc - bytecode of VX
// ------ development ----
// .vxs - bytecode json visual representation
// .steps - Steps
// .cmplx - Complex
// .stick - Stick
// .wit - Wit
