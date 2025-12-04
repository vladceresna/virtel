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

/// ------------------ Error type ----------------
#[derive(Debug, Clone)]
pub enum VMError {
    StackUnderflow,
    FrameError,
    DivisionByZero,
    InvalidOpcode,
    HeapError(String),
    UnknownFunction(usize),
    FunctionError(String), // error on user side
    NativeReferencesCastingError(String),
}
pub type VMResult<T> = Result<T, VMError>;

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

        let app_file_path = format!("{}/{}/{}.vxc", apps_dir, app_id, app_id);
        let app_file = fs::read_to_string(&app_file_path).expect("Failed to read .vxc file");
        {
            let mut app = self.data.write().unwrap();
            app.app_file = Some(app_file.clone());
        }
    }

    pub fn on_create(&self) {
        self.load_code_from_disk();
        log(
            Log::Info,
            format!("App created: {}", self.get_id()).as_str(),
        );
        self.set_status(AppStatus::Running);
        self.start_flow_from_function("on_create");
    }
    pub fn on_destroy(&self) {
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
            vm.ensure_slots(2);
            vm.get_variable("main", "VirtelApp", 0);
            vm.set_slot_double(1, 0.016);
        });
        vm.call(FunctionSignature::new_function(name, 0));
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

        app.on_create();

        app.on_destroy();

        assert_eq!(4, 4);
    }
}
