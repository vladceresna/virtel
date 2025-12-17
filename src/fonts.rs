use std::{any::Any, sync::Arc};

use skia_safe::{
    textlayout::{FontCollection, ParagraphBuilder, ParagraphStyle, TextStyle},
    wrapper::PointerWrapper,
    Canvas, Color, FontMgr, Paint, Point, RCHandle, Surface,
};

pub struct FontEngine {
    font_collection: FontCollection,
}

impl FontEngine {
    pub fn new() -> Self {
        let mut font_collection = FontCollection::new();
        font_collection.set_default_font_manager(FontMgr::default(), None);
        Self { font_collection }
    }

    pub fn draw_text(
        &self,
        surface: &mut Surface,
        text: &str,
        x: f32,
        y: f32,
        color: Color,
        font_size: f32,
    ) {
        let mut text_style = TextStyle::new();
        text_style.set_font_size(font_size);
        text_style.set_color(color);
        text_style.set_font_families(&[
            "sans-serif",
            "Cantarell",   // Fedora / GNOME default
            "DejaVu Sans", // Linux
            "Liberation Sans",
            "Ubuntu",
            "Noto Sans",
            "Roboto",
            "Arial",
        ]);

        let mut paragraph_style = ParagraphStyle::new();
        paragraph_style.set_text_style(&text_style);

        let mut builder = ParagraphBuilder::new(&paragraph_style, self.font_collection.clone());
        builder.add_text(text);

        let mut paragraph = builder.build();
        paragraph.layout(f32::INFINITY);

        println!(
            "Text layout size: {}x{}",
            paragraph.max_width(),
            paragraph.height()
        );

        paragraph.paint(surface.canvas(), Point::new(x, y));
        println!("FontDrawText")
    }
}

pub fn hex_to_color(hex_raw: &str) -> Color {
    // (0x, #)
    let hex = hex_raw.trim_start_matches("0x").trim_start_matches("#");

    // u8::from_str_radix
    let get_byte = |index: usize| -> u8 {
        if index + 2 > hex.len() {
            return 0;
        }
        u8::from_str_radix(&hex[index..index + 2], 16).unwrap_or(0)
    };

    if hex.len() == 8 {
        // AARRGGBB (8)
        let a = get_byte(0); // Alpha
        let r = get_byte(2); // Red
        let g = get_byte(4); // Green
        let b = get_byte(6); // Blue
        Color::from_argb(a, r, g, b)
    } else {
        // RRGGBB (6) -> Alpha = 255
        let r = get_byte(0);
        let g = get_byte(2);
        let b = get_byte(4);
        Color::from_argb(255, r, g, b)
    }
}
