use ruwren::{BasicFileLoader, ModuleLibrary, VMConfig, VMWrapper};

pub mod virtel_api;
pub mod virtel_fs_api;
pub mod virtel_net_api;
pub mod virtel_ui_api;

use crate::api::{
    virtel_api::{virtel, virtel_api_wren_bindings},
    virtel_fs_api::{virtel_fs, virtel_fs_api_wren_bindings},
    virtel_net_api::{virtel_net, virtel_net_api_wren_bindings},
    virtel_ui_api::{virtel_ui, virtel_ui_api_wren_bindings},
};

pub fn get_ready_apied_vm(base_dir: &str) -> VMWrapper {
    let script_loader = BasicFileLoader::new().base_dir(base_dir);

    let mut lib = ModuleLibrary::new();
    virtel::publish_module(&mut lib);
    virtel_fs::publish_module(&mut lib);
    virtel_ui::publish_module(&mut lib);
    virtel_net::publish_module(&mut lib);

    let vm = VMConfig::new()
        .library(&lib)
        .enable_relative_import(true)
        .script_loader(script_loader)
        .build();
    vm.interpret("virtel", virtel_api_wren_bindings()).unwrap();
    vm.interpret("virtel_fs", virtel_fs_api_wren_bindings())
        .unwrap();
    vm.interpret("virtel_ui", virtel_ui_api_wren_bindings())
        .unwrap();
    vm.interpret("virtel_net", virtel_net_api_wren_bindings())
        .unwrap();
    return vm;
}
