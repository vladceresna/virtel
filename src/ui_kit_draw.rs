use skia_safe::{Canvas, Color, Image, Paint, Rect, Surface};

use crate::fonts::FontEngine;

pub fn draw_cover(surface: &mut Surface, image: &Image) {
    let canvas_size = (surface.width() as f32, surface.height() as f32);
    let img_size = (image.width() as f32, image.height() as f32);

    let scale = (canvas_size.0 / img_size.0).max(canvas_size.1 / img_size.1);

    let new_w = img_size.0 * scale;
    let new_h = img_size.1 * scale;

    let left = (canvas_size.0 - new_w) / 2.0;
    let top = (canvas_size.1 - new_h) / 2.0;

    let dst_rect = Rect::from_xywh(left, top, new_w, new_h);

    surface
        .canvas()
        .draw_image_rect(image, None, dst_rect, &Paint::default());
}
