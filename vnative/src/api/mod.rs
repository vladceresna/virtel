use ruwren::{create_module, BasicFileLoader, ModuleLibrary, VMConfig, VMWrapper};

pub mod virtel_api;
pub mod virtel_net_api;
pub mod virtel_ui_api;

use crate::api::{
    virtel_api::{virtel, virtel_api_wren_bindings},
    virtel_net_api::{virtel_net, virtel_net_api_wren_bindings},
    virtel_ui_api::{virtel_ui, virtel_ui_api_wren_bindings},
};

pub fn get_ready_apied_vm(base_dir: &str) -> VMWrapper {
    let script_loader = BasicFileLoader::new().base_dir(base_dir);
    let mut lib = ModuleLibrary::new();

    virtel_ui::publish_module(&mut lib);

    let vm = VMConfig::new()
        .library(&lib)
        .enable_relative_import(true)
        .script_loader(script_loader)
        .build();
    vm.interpret("virtel_ui", virtel_ui_api_wren_bindings())
        .unwrap();
    return vm;
}

// pub fn get_ready_apied_vm(base_dir: &str) -> VMWrapper {
//     let script_loader = BasicFileLoader::new().base_dir(base_dir);

//     let mut lib = ModuleLibrary::new();
//     virtel::publish_module(&mut lib);
//     virtel_ui::publish_module(&mut lib);
//     virtel_net::publish_module(&mut lib);

//     let vm = VMConfig::new()
//         .library(&lib)
//         .enable_relative_import(true)
//         .script_loader(script_loader)
//         .build();
//     vm.interpret("virtel", virtel_api_wren_bindings()).unwrap();
//     vm.interpret("virtel_ui", virtel_ui_api_wren_bindings())
//         .unwrap();
//     vm.interpret("virtel_net", virtel_net_api_wren_bindings())
//         .unwrap();
//     return vm;
// }

#[test]
fn wren_isolated_ui_test() {
    let app_wren_code = r#"
        import "virtel_ui" for Window

        class MyApp {
            static start() {
                System.print("Creating window...")
                var win = Window.new("Isolated Test")
                System.print("Window created. Showing...")
                win.show()
                System.print("Window shown.")
            }
        }
    "#;

    let vm = crate::api::get_isolated_virtel_ui_vm();

    vm.interpret("main", app_wren_code)
        .expect("Error with interpreting app_wren_code");

    vm.execute(|vm| {
        vm.ensure_slots(1);
        vm.get_variable("main", "MyApp", 0);
    });
    vm.call(ruwren::FunctionSignature::new_function("start", 0))
        .expect("Error with function calling");
}

pub fn get_isolated_virtel_ui_vm() -> VMWrapper {
    let mut lib = ModuleLibrary::new();
    virtel_ui::publish_module(&mut lib);

    let vm = VMConfig::new().library(&lib).build();
    vm.interpret("virtel_ui", virtel_ui_api_wren_bindings())
        .unwrap();
    return vm;
}
