// src/events.rs
#[derive(Debug)]
pub enum VirtelEvent {
    CreateWindow {
        app_id: String,
        title: String,
        width: u32,
        height: u32,
    },
    DrawRect {
        app_id: String,
        x: i32,
        y: i32,
        w: i32,
        h: i32,
        color: u32, // ARGB
    },
    AppClosed {
        app_id: String,
    },
}
