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

        let mut paragraph_style = ParagraphStyle::new();
        paragraph_style.set_text_style(&text_style);

        let mut builder = ParagraphBuilder::new(&paragraph_style, self.font_collection.clone());
        builder.add_text(text);

        let mut paragraph = builder.build();
        paragraph.layout(f32::INFINITY);
        paragraph.paint(surface.canvas(), Point::new(x, y));
    }
}
