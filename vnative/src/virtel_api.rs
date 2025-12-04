use ruwren::{wren_impl, wren_module, WrenObject};

#[derive(WrenObject, Default)]
struct System {
    bar: f64,
}
#[wren_impl]
impl System {
    #[wren_impl(constructor)]
    fn constructor(&self, bar: f64) -> Result<SystemInstance, String> {
        Ok(SystemInstance { bar })
    }
    #[wren_impl(instance)]
    fn instance(&self) -> f64 {
        self.bar
    }
    fn static_fn(&self, num: f64) -> f64 {
        num + 5.0
    }
}
wren_module! {
    pub mod virtel {
        pub crate::virtel_api::System;
    }
}
