use skia_safe::Color;

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
    CreateUiContainer {},
    DrawText {
        app_id: String,
        text: String,
        color: Color,
        size: f32,
        x: f32,
        y: f32,
    },
    AppClosed {
        app_id: String,
    },
}
