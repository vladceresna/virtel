use skia_safe::{Color, Rect as SkRect};
use taffy::prelude::*;

pub enum WidgetType {
    Container {
        color: Color,
    },
    Text {
        content: String,
        size: f32,
        color: Color,
    },
    Button {
        label: String,
    },
}

pub struct UiNode {
    pub widget: WidgetType,
    pub children: Vec<UiNode>,
    pub style: Style, // Taffy (for example Flexbox/Grid)

    pub layout_rect: SkRect,
}

impl UiNode {
    pub fn new_container(color: Color) -> Self {
        Self {
            widget: WidgetType::Container { color },
            children: Vec::new(),
            style: Style::default(), // There can be padding/margin
            layout_rect: SkRect::default(),
        }
    }

    pub fn add_child(&mut self, child: UiNode) {
        self.children.push(child);
    }
}

pub struct LayoutEngine {
    taffy: TaffyTree,
}

impl LayoutEngine {
    pub fn new() -> Self {
        Self {
            taffy: TaffyTree::new(),
        }
    }

    // Taffy UiNode recursively
    fn build_taffy_tree(&mut self, node: &UiNode) -> NodeId {
        let mut child_ids = Vec::new();
        for child in &node.children {
            child_ids.push(self.build_taffy_tree(child));
        }

        self.taffy
            .new_with_children(node.style.clone(), &child_ids)
            .unwrap()
    }

    pub fn compute(&mut self, root_node: &mut UiNode, window_width: f32, window_height: f32) {
        self.taffy.clear();

        let root_id = self.build_taffy_tree(root_node);

        let available_space = Size {
            width: AvailableSpace::Definite(window_width),
            height: AvailableSpace::Definite(window_height),
        };

        self.taffy.compute_layout(root_id, available_space).unwrap();

        self.sync_layout(root_id, root_node, 0.0, 0.0);
    }

    fn sync_layout(&self, taffy_id: NodeId, node: &mut UiNode, parent_x: f32, parent_y: f32) {
        let layout = self.taffy.layout(taffy_id).unwrap();

        let abs_x = parent_x + layout.location.x;
        let abs_y = parent_y + layout.location.y;

        node.layout_rect = SkRect::from_xywh(abs_x, abs_y, layout.size.width, layout.size.height);

        let children_ids = self.taffy.children(taffy_id).unwrap();
        for (i, child) in node.children.iter_mut().enumerate() {
            self.sync_layout(children_ids[i], child, abs_x, abs_y);
        }
    }
}
