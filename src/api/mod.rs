use ruwren::{BasicFileLoader, ModuleLibrary, VMConfig, VMWrapper};

pub mod virtel_api;
pub mod virtel_fs_api;
pub mod virtel_net_api;
pub mod virtel_ui_api;

use crate::api::{
    virtel_api::virtel, virtel_fs_api::virtel_fs, virtel_net_api::virtel_net,
    virtel_ui_api::virtel_ui,
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
    vm.interpret("virtel", include_str!("../wren_api/virtel_api.wren"))
        .unwrap();
    vm.interpret("virtel/fs", include_str!("../wren_api/virtel_fs_api.wren"))
        .unwrap();
    vm.interpret("virtel/ui", include_str!("../wren_api/virtel_ui_api.wren"))
        .unwrap();
    vm.interpret(
        "virtel/net",
        include_str!("../wren_api/virtel_net_api.wren"),
    )
    .unwrap();
    return vm;
}
