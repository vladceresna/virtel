use std::{
    clone,
    fmt::{self, Debug},
    sync::{Arc, Mutex},
};

use once_cell::sync::Lazy;

use crate::{
    app::App,
    apps::{install_app, prepare_apps},
    settings::{FileSystem, Settings},
    vx,
};

#[derive(Debug, uniffi::Error)]
pub enum VirtelError {
    Message(String),
}
impl fmt::Display for VirtelError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            VirtelError::Message(msg) => write!(f, "{msg}"),
        }
    }
}

#[uniffi::export(with_foreign)]
pub trait UiApi: Send + Sync + Debug {
    fn create_window(&self, title: String, width: i64, height: i64) -> Result<String, VirtelError>;
    fn put_box(&self, node: String, id: String) -> Result<String, VirtelError>;
}

#[uniffi::export(with_foreign)]
pub trait SystemApi: Send + Sync + Debug {
    fn get_os_home_dir(&self) -> Result<String, VirtelError>;
}

#[uniffi::export]
pub fn get_virtel_center() -> Arc<VirtelCenter> {
    Arc::clone(&VIRTEL_CENTER)
}

pub static VIRTEL_CENTER: Lazy<Arc<VirtelCenter>> = Lazy::new(|| Arc::new(VirtelCenter::new()));

struct VirtelCenterData {
    apps: Vec<Arc<App>>,
    ui_api: Option<Arc<dyn UiApi>>,
    system_api: Option<Arc<dyn SystemApi>>,
    settings: Arc<Settings>,
}

#[derive(uniffi::Object)]
pub struct VirtelCenter {
    pub data: Mutex<VirtelCenterData>,
}

#[uniffi::export]
impl VirtelCenter {
    #[uniffi::constructor]
    pub fn new() -> VirtelCenter {
        VirtelCenter {
            data: Mutex::new(VirtelCenterData {
                apps: Vec::new(),
                ui_api: None,
                system_api: None,
                settings: Arc::new(Settings::new(FileSystem::new())),
            }),
        }
    }
    pub fn initialize(&self, ui_api: Arc<dyn UiApi>, system_api: Arc<dyn SystemApi>) {
        {
            let mut data = self.data.lock().unwrap();
            data.ui_api = Some(ui_api);
            data.system_api = Some(system_api);
        }
        prepare_apps();
        self.run_app("vladceresna.virtel.launcher".to_string());
        println!("VirtelCenter initialized.");
    }
}
impl VirtelCenter {
    pub fn get_ui_api(&self) -> Arc<dyn UiApi> {
        let data = self.data.lock().unwrap();
        data.ui_api.as_ref().map(Arc::clone).unwrap()
    }
    pub fn get_system_api(&self) -> Arc<dyn SystemApi> {
        let data = self.data.lock().unwrap();
        data.system_api.as_ref().map(Arc::clone).unwrap()
    }
    pub fn get_settings(&self) -> Arc<Settings> {
        let data = self.data.lock().unwrap();
        Arc::clone(&data.settings)
    }
    pub fn get_app_by_id(&self, app_id: String) -> Arc<App> {
        let data = self.data.lock().unwrap();

        let app = data
            .apps
            .iter()
            .find_map(|arc_app| {
                if arc_app.get_id() == app_id {
                    Some(arc_app)
                } else {
                    None
                }
            })
            .unwrap();
        return Arc::clone(app);
    }

    pub fn run_app(&self, app_id: String) {
        let app = self.get_app_by_id(app_id);
        app.on_create();
    }

    pub fn scan_apps(&self) {}
}

//tests
#[cfg(test)]
mod tests {
    use std::sync::{Arc, Mutex};

    use super::*;

    #[test]
    fn it_works() {
        let vc = Arc::new(Mutex::new(VirtelCenter::new()));
        vc.lock()
            .unwrap()
            .run_app("vladceresna.virtel.launcher".to_string());
        let vc2 = Arc::clone(&vc);

        let thread = std::thread::spawn(move || vc2.lock().unwrap());

        for _ in 0..5 {
            println!("Main thread is doing other work...");
            std::thread::sleep(std::time::Duration::from_millis(200));
        }
        vc.lock()
            .unwrap()
            .run_app("vladceresna.virtel.launcher".to_string());

        thread.join().unwrap();
        //assert_eq!(result, 4);
    }
}
