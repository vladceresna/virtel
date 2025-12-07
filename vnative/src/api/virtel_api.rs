use ruwren::{foreign_v2::WrenString, wren_impl, wren_module, WrenObject};

use crate::center::get_virtel_center;

#[derive(WrenObject, Default)]
pub struct Log {}
#[wren_impl]
impl Log {
    fn info(&self, msg: WrenString) {
        crate::log::log(crate::log::Log::Info, msg.into_string().unwrap().as_str());
    }
    fn success(&self, msg: WrenString) {
        crate::log::log(
            crate::log::Log::Success,
            msg.into_string().unwrap().as_str(),
        );
    }
    fn error(&self, msg: WrenString) {
        crate::log::log(crate::log::Log::Error, msg.into_string().unwrap().as_str());
    }
    fn warning(&self, msg: WrenString) {
        crate::log::log(
            crate::log::Log::Warning,
            msg.into_string().unwrap().as_str(),
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
        get_virtel_center()
    }
    #[allow(non_snake_case)]
    fn check(&self, id: i64) -> bool {}
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
