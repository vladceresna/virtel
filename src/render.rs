use crate::{
    fonts::FontEngine,
    ui_layout::{UiNode, WidgetType},
};
use skia_safe::{Canvas, Paint, Surface};

pub fn render_ui(surface: &mut Surface, node: &UiNode, fonts: &FontEngine) {
    let canvas = surface.canvas();
    match &node.widget {
        WidgetType::Container { color } => {
            let mut paint = Paint::default();
            paint.set_color(*color);
            paint.set_anti_alias(true);
            canvas.draw_rrect(node.layout_rect, &paint);
        }
        WidgetType::Text {
            content,
            size,
            color,
        } => {
            fonts.draw_text(
                surface,
                content,
                node.layout_rect.rect().x(),
                node.layout_rect.rect().y(),
                *color,
                *size,
            );
        }
        WidgetType::Button { label } => {
            // draw_modern_button(canvas, node.layout_rect, label, fonts, false);
        }
    }

    for child in &node.children {
        render_ui(surface, child, fonts);
    }
}
