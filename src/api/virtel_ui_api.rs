use ruwren::{foreign_v2::WrenString, wren_impl, wren_module, WrenObject};

use crate::center::get_virtel_center;

#[derive(WrenObject, Default)]
pub struct UI {}
#[wren_impl]
impl UI {
    #[allow(non_snake_case)]
    fn createWindow(&self, title: WrenString, width: i64, height: i64) -> String {
        println!("createWindow");
        get_virtel_center()
            .get_ui_api()
            .create_window(title.into_string().unwrap(), width, height)
            .unwrap()
    }
    // #[allow(non_snake_case)]
    // fn showWindow(&self, id: WrenString) -> String {
    //     get_virtel_center()
    //         .get_ui_api()
    //         .(id.into_string().unwrap(), width, height)
    //         .unwrap()
    // }
}

pub fn virtel_ui_api_wren_bindings() -> &'static str {
    return r#"
class UI {
    foreign static createWindow(title, width, height)
}
"#;
}

wren_module! {
    pub mod virtelui {
        pub crate::api::virtel_ui_api::UI;
    }
}
