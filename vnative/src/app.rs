use std::{sync::Mutex, thread::JoinHandle};



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
} impl App {
    pub fn new(id: String, name: String, version: String) -> Self {
        Self {
            data: Mutex::new(AppData {
                id,
                name,
                version,
                threads: Vec::new(),
            })
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
        println!("App {} created.", self.data.lock().unwrap().id);
    }
    pub fn on_destroy(&self) {
        
        println!("App {} destroyed.", self.data.lock().unwrap().id);
    }
}