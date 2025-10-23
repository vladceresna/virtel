use std::{sync::Mutex, thread::JoinHandle};

use crate::{center::get_virtel_center, vx::VM};

#[derive(Debug, Default)]
struct AppData {
    id: String,
    name: String,
    version: String,
    threads: Vec<JoinHandle<()>>,
}

#[derive(Debug)]
pub struct App {
    data: Mutex<AppData>,
}
impl App {
    pub fn new(id: String, name: String, version: String) -> Self {
        Self {
            data: Mutex::new(AppData {
                id,
                name,
                version,
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
