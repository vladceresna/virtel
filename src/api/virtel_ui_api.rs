use ruwren::{foreign_v2::WrenString, wren_impl, wren_module, WrenObject};

use crate::{center::get_virtel_center, fonts::hex_to_color};

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
    #[allow(non_snake_case)]
    fn drawText(&self, text: WrenString, color: WrenString, size: i64, x: i64, y: i64) -> String {
        println!("drawText");

        get_virtel_center()
            .get_ui_api()
            .draw_text(
                text.into_string().unwrap(),
                hex_to_color(color.into_string().unwrap().as_str()),
                size as f32,
                x as f32,
                y as f32,
            )
            .unwrap()
    }
}

wren_module! {
    pub mod virtel_ui {
        pub crate::api::virtel_ui_api::UI;
    }
}
