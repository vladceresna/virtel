use ruwren::{foreign_v2::WrenString, wren_impl, wren_module, WrenObject};

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
wren_module! {
    pub mod virtel {
        pub crate::virtel_api::Log;
        pub crate::virtel_api::VirtelApp;
        pub crate::virtel_api::VirtelPlugin;
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
        println!("Rust: Base VirtelApp.start() called. Did you forget to override it?");
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
"#;
}
