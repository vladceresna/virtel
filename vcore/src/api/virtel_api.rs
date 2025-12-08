use ruwren::{foreign_v2::WrenString, wren_impl, wren_module, WrenObject};

use crate::{app::get_current_app_id, center::get_virtel_center};

#[derive(WrenObject, Default)]
pub struct Log {}
#[wren_impl]
impl Log {
    fn info(&self, msg: WrenString) {
        let app_id = get_current_app_id().unwrap_or("unknown".to_string());
        crate::log::log(
            crate::log::Log::Info,
            format!("[{}] {}", app_id, msg.into_string().unwrap()).as_str(),
        );
    }
    fn success(&self, msg: WrenString) {
        let app_id = get_current_app_id().unwrap_or("unknown".to_string());
        crate::log::log(
            crate::log::Log::Success,
            format!("[{}] {}", app_id, msg.into_string().unwrap()).as_str(),
        );
    }
    fn error(&self, msg: WrenString) {
        let app_id = get_current_app_id().unwrap_or("unknown".to_string());
        crate::log::log(
            crate::log::Log::Error,
            format!("[{}] {}", app_id, msg.into_string().unwrap()).as_str(),
        );
    }
    fn warning(&self, msg: WrenString) {
        let app_id = get_current_app_id().unwrap_or("unknown".to_string());
        crate::log::log(
            crate::log::Log::Warning,
            format!("[{}] {}", app_id, msg.into_string().unwrap()).as_str(),
        );
    }
}

#[derive(WrenObject, Default)]
pub struct VirtelApp {}
#[wren_impl]
impl VirtelApp {
    fn start(&self) {
        println!("Rust: Base VirtelApp.start() called. Did you forget to override it?");
    }
}
#[derive(WrenObject, Default)]
pub struct VirtelPlugin {}
#[wren_impl]
impl VirtelPlugin {
    fn start(&self) {
        println!("Rust: Base VirtelPlugin.start() called. Did you forget to override it?");
    }
}

#[derive(WrenObject, Default)]
pub struct Center {}
#[wren_impl]
impl Center {
    #[allow(non_snake_case)]
    fn startApp(&self, id: WrenString) {
        get_virtel_center().run_app(id.into_string().unwrap());
    }
}
#[derive(WrenObject, Default)]
pub struct Permissions {}
#[wren_impl]
impl Permissions {
    #[allow(non_snake_case)]
    fn request(&self, id: i64) {
        let app_id = get_current_app_id().unwrap_or("unknown".to_string());
        let app = get_virtel_center().get_app_by_id(app_id);
        app.request_permissions(id);
    }
    #[allow(non_snake_case)]
    fn check(&self, id: i64) -> bool {
        let app_id = get_current_app_id().unwrap_or("unknown".to_string());
        let app = get_virtel_center().get_app_by_id(app_id);
        app.check_permissions(id)
    }
}

pub fn virtel_api_wren_bindings() -> &'static str {
    return r#"
class Log {
    foreign static info(msg)
    foreign static success(msg)
    foreign static error(msg)
    foreign static warning(msg)
}
class VirtelApp {
    foreign static start()
}
class VirtelPlugin {
    foreign static start()
}
class Center {
    foreign static startApp(id)
}
class Permissions {
    foreign static request(id)
    foreign static check(id)
}
"#;
}

wren_module! {
    pub mod virtel {
        pub crate::api::virtel_api::Log;
        pub crate::api::virtel_api::VirtelApp;
        pub crate::api::virtel_api::VirtelPlugin;
        pub crate::api::virtel_api::Center;
        pub crate::api::virtel_api::Permissions;
    }
}
