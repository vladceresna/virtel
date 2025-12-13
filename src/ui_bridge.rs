use crate::app::get_current_app_id;
use crate::center::UiApi;
// src/ui_bridge.rs
use crate::events::VirtelEvent;
use crate::VirtelError;
use std::fmt::Debug;
use winit::event_loop::EventLoopProxy;

#[derive(Clone)]
pub struct WinitUiBridge {
    proxy: EventLoopProxy<VirtelEvent>,
}

impl WinitUiBridge {
    pub fn new(proxy: EventLoopProxy<VirtelEvent>) -> Self {
        Self { proxy }
    }
}

impl Debug for WinitUiBridge {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "WinitUiBridge")
    }
}

impl UiApi for WinitUiBridge {
    fn create_window(&self, title: String, width: i64, height: i64) -> Result<String, VirtelError> {
        let app_id =
            get_current_app_id().ok_or(VirtelError::Message("No current app id context".into()))?;

        self.proxy
            .send_event(VirtelEvent::CreateWindow {
                app_id: app_id.clone(),
                title,
                width: width as u32,
                height: height as u32,
            })
            .map_err(|_| VirtelError::Message("Event loop is closed".into()))?;

        Ok(app_id)
    }

    fn put_box(&self, _node: String, _id: String) -> Result<String, VirtelError> {
        let app_id = get_current_app_id().unwrap_or("unknown".to_string());

        self.proxy
            .send_event(VirtelEvent::DrawRect {
                app_id,
                x: 50,
                y: 50,
                w: 100,
                h: 100,
                color: 0xFFFF0000, // Red
            })
            .unwrap();

        Ok("box_drawn".to_string())
    }
}
