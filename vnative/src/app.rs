use ruwren::{FunctionSignature, ModuleLibrary, VMConfig};
use std::{
    fs,
    sync::{Arc, RwLock},
};
use tokio::task::JoinHandle;

use crate::{
    center::get_virtel_center,
    log::{log, Log},
    permissions::Permissions,
    tokio_setup::get_tokio,
    virtel_api::virtel,
};

use serde::{Deserialize, Serialize};

/// ------------------- APP STRUCTURE -------------------

#[derive(Debug)]
pub enum AppStatus {
    Running,
    Background,
    Stopped,
    Paused,
    Error(String),
}
#[derive(Debug, Serialize, Deserialize)]
pub struct AppConfig {
    id: String,
    name: String,
    version: String,
    icon_path: String,
}
pub struct App {
    app_config: AppConfig,    // config.json
    app_file: Option<String>, // {app_id}.wren
    status: AppStatus,
    permissions: Permissions,
}
pub struct AppElement {
    data: RwLock<App>,
}
impl AppElement {
    pub fn new(app_id: String) -> Self {
        let apps_dir = get_virtel_center()
            .get_settings()
            .filesystem
            .apps_dir
            .clone();

        let this_app_config = format!("{}/{}/config.json", apps_dir, app_id);
        let this_app_config_content = fs::read_to_string(this_app_config).unwrap();
        let app_config: AppConfig = serde_json::from_str(this_app_config_content.as_str()).unwrap();

        Self {
            data: RwLock::new(App {
                app_config: app_config,
                app_file: None,
                status: AppStatus::Stopped,
                permissions: Permissions::new(),
            }),
        }
    }
    pub fn get_id(&self) -> String {
        self.data.read().unwrap().app_config.id.clone()
    }
    pub fn get_name(&self) -> String {
        self.data.read().unwrap().app_config.name.clone()
    }
    pub fn get_version(&self) -> String {
        self.data.read().unwrap().app_config.version.clone()
    }
    fn set_status(&self, status: AppStatus) {
        self.data.write().unwrap().status = status
    }
    pub fn load_code_from_disk(&self) {
        let apps_dir = get_virtel_center()
            .get_settings()
            .filesystem
            .apps_dir
            .clone();

        let app_id = self.get_id();

        let app_file_path = format!("{}/{}/code/app.wren", apps_dir, app_id);
        let app_file = fs::read_to_string(&app_file_path).expect("Failed to read .wren file");
        {
            let mut app = self.data.write().unwrap();
            app.app_file = Some(app_file.clone());
        }
    }

    pub fn start(&self) {
        self.load_code_from_disk();
        log(
            Log::Info,
            format!("App created: {}", self.get_id()).as_str(),
        );
        self.set_status(AppStatus::Running);
        self.start_flow_from_function("start");
    }
    pub fn destroy(&self) {
        println!("App {} destroyed.", self.get_id());
        self.set_status(AppStatus::Stopped);
    }
    pub fn start_flow_from_function(&self, name: &str) {
        let app_file = { self.data.read().unwrap().app_file.clone().unwrap() };

        let mut lib = ModuleLibrary::new();
        virtel::publish_module(&mut lib);

        let vm = VMConfig::new().library(&lib).build();

        vm.interpret("main", app_file).unwrap();
        vm.execute(|vm| {
            vm.ensure_slots(1);
            vm.get_variable("main", "VirtelApp", 0);
        });
        vm.call(FunctionSignature::new_function(name, 0))
            .expect("Error with function calling");
    }
    pub fn install_app(path_to_lpp: String) {
        todo!();
    }
}

#[cfg(test)]
mod tests {

    use super::*;
    #[test]
    fn it_works() {
        let app = AppElement::new("vladceresna.virtel.launcher".to_string());

        app.start();

        app.destroy();

        assert_eq!(4, 4);
    }
    #[test]
    fn wren_works() {
        let app_file_path =
            "/home/vladceresna/.virtel/0/sys/apps/vladceresna.virtel.launcher/code/app.wren"
                .to_string();
        let app_file = fs::read_to_string(&app_file_path).expect("Failed to read .wren file");

        let mut lib = ModuleLibrary::new();
        virtel::publish_module(&mut lib);

        let vm = VMConfig::new().library(&lib).build();
        vm.interpret(
            "virtel",
            r#"
            class Log {
                foreign static info(msg)
                foreign static success(msg)
                foreign static error(msg)
                foreign static warning(msg)
            }
            "#,
        )
        .unwrap();
        vm.interpret("main", app_file)
            .expect("Error with interpreting app_file");
        vm.execute(|vm| {
            vm.ensure_slots(1);
            vm.get_variable("main", "VirtelApp", 0);
        });
        vm.call(FunctionSignature::new_function("start", 0))
            .expect("Error with function calling");
    }
}
