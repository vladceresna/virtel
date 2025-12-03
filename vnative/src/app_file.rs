use std::{
    array::from_fn,
    collections::VecDeque,
    sync::{Arc, RwLock},
    thread,
};

use bincode::{config, Decode, Encode};
use serde::{Deserialize, Serialize};

use crate::data::Constant;

#[derive(Serialize, Deserialize, Encode, Decode, Clone)]
pub struct AppFile {
    magic: [u8; 4],               // "VXCL"
    version: u8,                  // 4...5...6...7 as Virtel versions
    entry_points: Vec<u64>, // it is only directive to system, but user can select another function to entry in
    global_consts: Vec<Constant>, // data, that exists on start of instructions execution
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
    pub fn get_global_consts(&self) -> &Vec<Constant> {
        &self.global_consts // todo or .clone() or &self
    }
    pub fn get_entry_point_key(&self, index: usize) -> u64 {
        *self
            .entry_points
            .get(index)
            .expect("Entry point index out of bounds")
    }
    pub fn vec_test(&self) {
        //let public_data: Arc<RwLock<[Constant; 256]>> =
        //    Arc::new(RwLock::new(from_fn(|_| Constant::Null)));

        //public_data.write().unwrap()[0] = Constant::Bool(true);
        //public_data.read().unwrap().get(0).unwrap();

        // let some_vec: Arc<Vec<Constant>> = Arc::new(vec![]);
        // let arc1 = some_vec.clone();
        // thread::spawn(move || {
        //     arc1.get(0).unwrap();
        // });
        // let arc2 = some_vec.clone();
        // thread::spawn(move || {
        //     arc2.get(0).unwrap();
        // });
        //

        return;
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
