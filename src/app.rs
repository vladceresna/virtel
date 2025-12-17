use ruwren::{FunctionSignature, ModuleLibrary, VMConfig};
use std::{
    collections::HashSet,
    fs,
    sync::{Arc, RwLock},
    thread,
};
use tokio::task::JoinHandle;

use crate::{
    api::get_ready_apied_vm,
    app,
    center::get_virtel_center,
    log::{log, Log},
    tokio_setup::get_tokio,
};

use serde::{Deserialize, Serialize};

use std::cell::RefCell;
use std::thread_local;

// --- Now we know app_id in foreign bindings from Rust to Wren ----
thread_local! {
    static CURRENT_APP_ID: RefCell<Option<String>> = RefCell::new(None);
}
pub fn set_current_app_id(app_id: &str) {
    CURRENT_APP_ID.with(|id| {
        *id.borrow_mut() = Some(app_id.to_string());
    });
}
pub fn get_current_app_id() -> Option<String> {
    CURRENT_APP_ID.with(|id| id.borrow().clone())
}
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
    main_class: String,
}
pub struct App {
    app_config: AppConfig,    // config.json
    app_file: Option<String>, // {app_id}.wren
    status: AppStatus,
    permissions: HashSet<i64>,
}
pub struct AppElement {
    data: RwLock<App>,
}
impl AppElement {
    pub fn new(app_id: String, apps_dir: &str) -> Self {
        let this_app_config = format!("{}/{}/config.json", apps_dir, app_id);

        let this_app_config_content = std::fs::read_to_string(&this_app_config)
            .unwrap_or_else(|_| panic!("Config not found: {}", this_app_config));

        let app_config: AppConfig = serde_json::from_str(&this_app_config_content).unwrap();

        Self {
            data: RwLock::new(App {
                app_config: app_config,
                app_file: None,
                status: AppStatus::Stopped,
                permissions: HashSet::new(),
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
    pub fn get_main_class(&self) -> String {
        self.data.read().unwrap().app_config.main_class.clone()
    }
    fn set_status(&self, status: AppStatus) {
        self.data.write().unwrap().status = status
    }
    pub fn load_code_from_disk(&self, apps_dir: &str) {
        let app_id = self.get_id();

        let app_file_path = format!("{}/{}/code/app.wren", apps_dir, app_id);
        let app_file = fs::read_to_string(&app_file_path).expect("Failed to read .wren file");
        {
            let mut app = self.data.write().unwrap();
            app.app_file = Some(app_file.clone());
        }
    }

    pub fn start(&self, apps_dir: &str) {
        self.load_code_from_disk(apps_dir);
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
        let main_class = self.get_main_class();
        let apps_dir = get_virtel_center()
            .get_settings()
            .filesystem
            .apps_dir
            .clone();

        let app_id = self.get_id();

        let app_file = { self.data.read().unwrap().app_file.clone().unwrap() };

        let name = name.to_string();

        thread::spawn(move || {
            set_current_app_id(&app_id);

            let vm = get_ready_apied_vm(format!("{}/{}/code", apps_dir, app_id).as_str());

            vm.interpret("main", app_file)
                .expect("Error with interpreting app_file");
            vm.execute(|vm| {
                vm.ensure_slots(1);
                vm.get_variable("main", main_class, 0);
            });

            vm.call(FunctionSignature::new_function(name, 0))
                .expect("Error with function calling");
        });
    }
    pub fn install_app(path_to_lpp: String) {
        todo!();
    }
    pub fn request_permissions(&self, id: i64) {
        self.data.write().unwrap().permissions.insert(id);
    }
    pub fn check_permissions(&self, id: i64) -> bool {
        self.data.read().unwrap().permissions.contains(&id)
    }
}

#[cfg(test)]
mod tests {

    use crate::api::{self, get_ready_apied_vm};

    use super::*;
    #[test]
    fn it_works() {
        let apps_dir = "/home/vladceresna/.virtel/0/sys/apps";
        let app = AppElement::new("vladceresna.virtel.launcher".to_string(), &apps_dir);

        app.start(&apps_dir);

        app.destroy();

        assert_eq!(4, 4);
    }
    #[test]
    fn wren_works() {
        let app_file_path =
            "/home/vladceresna/.virtel/0/sys/apps/vladceresna.virtel.launcher/code/app.wren"
                .to_string();
        let app_file = fs::read_to_string(&app_file_path).expect("Failed to read .wren file");

        let vm = get_ready_apied_vm(
            format!("/home/vladceresna/.virtel/0/sys/apps/vladceresna.virtel.launcher/code")
                .as_str(),
        );

        vm.interpret("main", app_file)
            .expect("Error with interpreting app_file");
        vm.execute(|vm| {
            vm.ensure_slots(1);
            vm.get_variable("main", "MyApp", 0);
        });
        vm.call(FunctionSignature::new_function("start", 0))
            .expect("Error with function calling");
    }
}
