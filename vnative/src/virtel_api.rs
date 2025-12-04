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
    }
}
