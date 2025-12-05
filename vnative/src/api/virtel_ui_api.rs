use ruwren::{foreign_v2::WrenString, wren_impl, wren_module, WrenObject};

use crate::center::get_virtel_center;

#[derive(WrenObject, Default)]
pub struct Window {
    name: String,
}

#[wren_impl]
impl Window {
    #[wren_impl(constructor)]
    fn new(&mut self, name: WrenString) -> Result<WindowInstance, String> {
        Ok(WindowInstance {
            name: name.into_string().unwrap(),
        })
    }

    #[wren_impl(instance)]
    fn show(&self) {
        println!("Showing window named: {}", self.name);
        get_virtel_center()
            .get_ui_api()
            .create_window(self.name.clone(), 100, 100)
            .unwrap();
    }
}

pub fn virtel_ui_api_wren_bindings() -> &'static str {
    return r#"
foreign class Window {
    construct new(name) {}
    foreign show()
}
"#;
}
wren_module! {
    pub mod virtel_ui {
        pub crate::api::virtel_ui_api::Window;
    }
}
