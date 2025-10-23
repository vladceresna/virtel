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
    fn create_window(&self, title: String) -> Result<String, VirtelError>;
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
    running_apps: Vec<Arc<App>>,
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
                running_apps: Vec::new(),
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

    pub fn run_app(&self, app_id: String) {
        let mut data = self.data.lock().unwrap();

        println!("Running app: {}", app_id);
        let app = App::new(app_id, "Virtel Launcher".to_string(), "0.1.0".to_string());
        let arc_app = Arc::new(app);

        arc_app.on_create();
        data.running_apps.push(arc_app);
    }

    pub fn stop_app(&self, app_id: String) {
        let mut data = self.data.lock().unwrap();

        println!("Stopping app: {}", app_id);
        data.running_apps.retain(|app_arc| {
            if app_arc.get_id() == app_id {
                app_arc.on_destroy();
                false
            } else {
                true
            }
        });
    }

    pub fn get_running_app_ids(&self) -> Vec<String> {
        let data = self.data.lock().unwrap();
        data.running_apps
            .iter()
            .map(|app_arc| app_arc.get_id())
            .collect()
    }
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

        let thread = std::thread::spawn(move || {
            vc2.lock()
                .unwrap()
                .stop_app("vladceresna.virtel.launcher".to_string());
        });

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
