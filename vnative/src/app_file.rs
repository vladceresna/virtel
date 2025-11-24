use slotmap::{Key, SlotMap};

use crate::{app::App, data::Constant};

pub struct ApplicationFile {
    magic: [u8; 4], // "VCLL"
    version: u8,    // 4...5...6...7 as Virtel versions
    entry_point: Key,
    constants: SlotMap<Key, Constant>,
}
impl ApplicationFile {
    pub fn to_app(&self) -> App {}
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
