use std::{fs, sync::Mutex, thread::JoinHandle};

use serde_json::Value;

use crate::center::get_virtel_center;

#[derive(Debug)]
pub enum AppStatus {
    Running,
    Background,
    Stopped,
    Paused,
    Error,
}
impl Default for AppStatus {
    fn default() -> Self {
        AppStatus::Stopped
    }
}

#[derive(Debug, Default)]
struct AppData {
    id: String,
    name: String,
    version: String,
    icon_path: String,
    status: AppStatus,
    threads: Vec<JoinHandle<()>>,
}

#[derive(Debug)]
pub struct App {
    data: Mutex<AppData>,
}
impl App {
    pub fn new(app_id: String) -> Self {
        //read from appid/config.json

        let apps_dir = get_virtel_center()
            .get_settings()
            .filesystem
            .apps_dir
            .clone();
        let this_app_config = format!("{}/{}/config.json", apps_dir, app_id);
        let this_app_config_content = fs::read_to_string(this_app_config).unwrap();

        let c: Value = serde_json::from_str(this_app_config_content.as_str()).unwrap();

        Self {
            data: Mutex::new(AppData {
                id: app_id,
                name: c["name"].to_string(),
                version: c["version"].to_string(),
                icon_path: c["iconPath"].to_string(),
                status: AppStatus::default(),
                threads: Vec::new(),
            }),
        }
    }
    pub fn get_id(&self) -> String {
        self.data.lock().unwrap().id.clone()
    }
    pub fn get_name(&self) -> String {
        self.data.lock().unwrap().name.clone()
    }
    pub fn get_version(&self) -> String {
        self.data.lock().unwrap().version.clone()
    }
    pub fn on_create(&self) {
        let apps_dir = get_virtel_center()
            .get_settings()
            .filesystem
            .apps_dir
            .clone();
        let app_id = self.data.lock().unwrap().id.clone();
        let app_file = apps_dir + "/" + app_id.as_str() + "/code/" + app_id.as_str() + ".vc";
        //VM::new(chunk);
        //TODO starting

        println!("App {} created.", app_id);
    }
    pub fn on_destroy(&self) {
        println!("App {} destroyed.", self.data.lock().unwrap().id);
    }
}
