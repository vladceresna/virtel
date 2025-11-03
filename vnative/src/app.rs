use serde_json::Value;
use std::{error::Error, fs, sync::Mutex};
use tokio::task::JoinHandle;

use crate::{
    center::get_virtel_center,
    chunk_utils::vc_to_chunk,
    log::log,
    tokio_setup::{get_tokio, TOKIO},
    vx::{Chunk, VM},
};

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
        let app_file = format!("{}/{}/code/{}.vc", apps_dir, app_id, app_id);

        let vc = fs::read(&app_file).expect("Failed to read .vc file");
        let chunk = vc_to_chunk(vc);

        log(
            crate::log::Log::Info,
            format!("App crated: {}", app_id).as_str(),
        );
        self.data.lock().unwrap().status = AppStatus::Running;
        // Starting VM
        let handle = get_tokio().spawn(async move {
            let mut vm = VM::new(&chunk);
            vm.run();
        });
        self.data.lock().unwrap().threads.push(handle);
    }
    pub fn on_destroy(&self) {
        println!("App {} destroyed.", self.data.lock().unwrap().id);
        self.data.lock().unwrap().status = AppStatus::Stopped;
    }
    // pub fn run_new_thread(future: F) -> Result<(), Error> {
    //     let handle = get_tokio().spawn(future);
    //     self.data.lock().unwrap().threads.push(handle);
    // }
}
