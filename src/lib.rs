use std::collections::HashMap;
use std::io::Write;
use std::num::NonZeroU32;
use std::rc::Rc;
use std::sync::Arc;

use glutin::{
    config::{ConfigTemplateBuilder, GlConfig},
    context::{ContextAttributesBuilder, NotCurrentGlContext, PossiblyCurrentContext},
    display::GetGlDisplay,
    prelude::*,
    surface::{Surface as GlutinSurface, WindowSurface},
};
use glutin_winit::{DisplayBuilder, GlWindow};
use raw_window_handle::HasWindowHandle;

use skia_safe::{
    gpu::{self, gl::FramebufferInfo, BackendRenderTarget, SurfaceOrigin},
    Color, ColorType, Paint, Rect, Surface,
};

use winit::event::{TouchPhase, WindowEvent};
use winit::event_loop::{ActiveEventLoop, ControlFlow, EventLoop, EventLoopProxy};
use winit::window::{Window, WindowId};
use winit::{application::ApplicationHandler, event::Event};

mod api;
mod app;
mod apps;
mod center;
mod events;
mod fonts;
mod log;
mod net;
mod render;
mod settings;
mod tokio_setup;
mod ui_bridge;
mod ui_layout;

use crate::{
    center::{get_virtel_center, SystemApi, VirtelError},
    ui_layout::{LayoutEngine, UiNode},
};
use events::VirtelEvent;
use fonts::FontEngine;
use ui_bridge::WinitUiBridge;

#[cfg(target_os = "android")]
fn init_logging() {
    android_logger::init_once(
        android_logger::Config::default()
            //.with_max_level(log::Level::Trace)
            .with_tag("virtel"),
    );
}

#[cfg(not(target_os = "android"))]
fn init_logging() {
    simple_logger::SimpleLogger::new().init().unwrap();
}

#[cfg(target_os = "android")]
use winit::platform::android::activity::AndroidApp;
#[cfg(target_os = "android")]
use winit::platform::android::EventLoopBuilderExtAndroid;

#[cfg(target_os = "android")]
#[no_mangle]
pub fn android_main(app: AndroidApp) {
    init_logging();

    let mut event_loop = EventLoop::with_user_event()
        .with_android_app(app)
        .build()
        .unwrap();

    let mut app = VirtelOS::new(event_loop.create_proxy());
    event_loop.run_app(&mut app).unwrap();
}

fn log_now(msg: &str) {
    println!("{}", msg);
    std::io::stdout().flush().unwrap();
}

struct VirtualAppWindow {
    rect: Rect,
    surface: Surface,
    #[allow(dead_code)]
    title: String,

    ui_root: UiNode,
    layout_engine: LayoutEngine,
}

struct VirtelOS {
    window: Option<Rc<Window>>,
    gl_context: Option<PossiblyCurrentContext>,
    gl_surface: Option<GlutinSurface<WindowSurface>>,
    gr_context: Option<gpu::DirectContext>,
    app_windows: HashMap<String, VirtualAppWindow>,
    core_initialized: bool,
    proxy: EventLoopProxy<VirtelEvent>,
    font_engine: Option<FontEngine>,
    cursor_position: (f64, f64),
    dragging_window: Option<String>,
    drag_offset: (f32, f32),
}

impl VirtelOS {
    fn new(proxy: EventLoopProxy<VirtelEvent>) -> Self {
        Self {
            window: None,
            gl_context: None,
            gl_surface: None,
            gr_context: None,
            app_windows: HashMap::new(),
            core_initialized: false,
            proxy,
            font_engine: None,
            cursor_position: (0.0, 0.0),
            dragging_window: None,
            drag_offset: (0.0, 0.0),
        }
    }
}

impl ApplicationHandler<VirtelEvent> for VirtelOS {
    fn resumed(&mut self, event_loop: &ActiveEventLoop) {
        if self.window.is_none() {
            log_now("OS: Initializing OpenGL Renderer...");

            let win_attr = Window::default_attributes()
                .with_title("Virtel OS (GPU)")
                .with_inner_size(winit::dpi::LogicalSize::new(800.0, 600.0))
                .with_visible(true)
                .with_decorations(false);

            let template = ConfigTemplateBuilder::new()
                .with_alpha_size(8)
                .with_transparency(true);

            let display_builder = DisplayBuilder::new().with_window_attributes(Some(win_attr));

            let (window, gl_config) = display_builder
                .build(event_loop, template, |configs| {
                    configs
                        .reduce(|accum, config| {
                            if config.num_samples() > accum.num_samples() {
                                config
                            } else {
                                accum
                            }
                        })
                        .unwrap()
                })
                .unwrap();

            let window = Rc::new(window.unwrap());
            let raw_window_handle = window.window_handle().unwrap().as_raw();

            let gl_display = gl_config.display();
            let context_attributes = ContextAttributesBuilder::new().build(Some(raw_window_handle));

            let not_current_gl_context = unsafe {
                gl_display
                    .create_context(&gl_config, &context_attributes)
                    .expect("Failed to create GL context")
            };

            let attrs = window
                .build_surface_attributes(Default::default())
                .expect("Failed to build surface attrs");
            let gl_surface = unsafe {
                gl_display
                    .create_window_surface(&gl_config, &attrs)
                    .expect("Failed to create GL surface")
            };

            let gl_context = not_current_gl_context.make_current(&gl_surface).unwrap();

            let interface = skia_safe::gpu::gl::Interface::new_load_with(|name| {
                if let Ok(cname) = std::ffi::CString::new(name) {
                    gl_display.get_proc_address(&cname) as *const _
                } else {
                    std::ptr::null()
                }
            })
            .expect("Failed to create native Skia GL interface");

            let gr_context = gpu::DirectContext::new_gl(interface, None)
                .expect("Failed to create Skia DirectContext");

            self.window = Some(window);
            self.gl_context = Some(gl_context);
            self.gl_surface = Some(gl_surface);
            self.gr_context = Some(gr_context);
            self.font_engine = Some(FontEngine::new());

            log_now("OS: GPU Initialized Successfully.");

            if !self.core_initialized {
                log_now("OS: Spawning Core Thread...");
                let proxy = self.proxy.clone();
                let ui_api = Arc::new(WinitUiBridge::new(proxy));

                struct MockSystem;
                impl SystemApi for MockSystem {
                    fn get_os_home_dir(&self) -> Result<String, VirtelError> {
                        Ok(".".into())
                    }
                }
                impl std::fmt::Debug for MockSystem {
                    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
                        write!(f, "Sys")
                    }
                }
                let sys_api = Arc::new(MockSystem);

                std::thread::Builder::new()
                    .name("VirtelCore".into())
                    .spawn(move || {
                        let center = get_virtel_center();
                        let _ = std::panic::catch_unwind(move || {
                            center.initialize(ui_api, sys_api);
                        });
                    })
                    .unwrap();

                self.core_initialized = true;
            }
        }
    }

    fn user_event(&mut self, _event_loop: &ActiveEventLoop, event: VirtelEvent) {
        match event {
            VirtelEvent::CreateWindow {
                app_id,
                title,
                width,
                height,
            } => {
                log_now(&format!("OS: [Event] CreateWindow {}", app_id));

                // Raster Surface (CPU)
                let mut surface =
                    skia_safe::surfaces::raster_n32_premul((width as i32, height as i32))
                        .expect("Failed to create app surface");

                {
                    let canvas = surface.canvas();
                    canvas.clear(Color::WHITE);

                    let mut hp = Paint::default();
                    hp.set_color(Color::LIGHT_GRAY);
                    canvas.draw_rect(Rect::from_xywh(0.0, 0.0, width as f32, 30.0), &hp);

                    if let Some(fonts) = &self.font_engine {
                        fonts.draw_text(&mut surface, &title, 10.0, 5.0, Color::BLACK, 20.0);
                        fonts.draw_text(
                            &mut surface,
                            "GPU Accelerated OS",
                            10.0,
                            50.0,
                            Color::DARK_GRAY,
                            16.0,
                        );
                    }
                }

                let app_win = VirtualAppWindow {
                    rect: Rect::from_xywh(50.0, 50.0, width as f32, height as f32),
                    surface: surface,
                    title,
                    ui_root: UiNode::new_container(Color::WHITE),
                    layout_engine: LayoutEngine::new(),
                };
                self.app_windows.insert(app_id, app_win);
            }
            VirtelEvent::DrawRect {
                app_id,
                x,
                y,
                w,
                h,
                color: _,
            } => {
                if let Some(app_win) = self.app_windows.get_mut(&app_id) {
                    let mut s = app_win.surface.clone();
                    let canvas = s.canvas();
                    let mut paint = Paint::default();
                    paint.set_color(Color::BLACK);
                    canvas.draw_rect(
                        Rect::from_xywh(x as f32, y as f32, w as f32, h as f32),
                        &paint,
                    );
                }
            }
            _ => {}
        }
        if let Some(w) = &self.window {
            w.request_redraw();
        }
    }

    fn window_event(&mut self, event_loop: &ActiveEventLoop, _id: WindowId, event: WindowEvent) {
        match event {
            WindowEvent::CloseRequested => event_loop.exit(),

            WindowEvent::Resized(size) => {
                if size.width > 0 && size.height > 0 {
                    if let (Some(gl_surface), Some(gl_context)) =
                        (&self.gl_surface, &self.gl_context)
                    {
                        gl_surface.resize(
                            gl_context,
                            NonZeroU32::new(size.width).unwrap(),
                            NonZeroU32::new(size.height).unwrap(),
                        );
                    }
                    if let Some(w) = &self.window {
                        w.request_redraw();
                    }
                }
            }
            WindowEvent::RedrawRequested => {
                if let (Some(gl_context), Some(gl_surface), Some(window), Some(gr_context)) = (
                    &self.gl_context,
                    &self.gl_surface,
                    &self.window,
                    &mut self.gr_context,
                ) {
                    // 1. Make Current
                    let _ = gl_context.make_current(gl_surface);

                    let size = window.inner_size();
                    if size.width == 0 || size.height == 0 {
                        return;
                    }

                    // 2. GPU Canvas
                    let fb_info = FramebufferInfo {
                        fboid: 0,
                        format: skia_safe::gpu::gl::Format::RGBA8.into(),
                        protected: skia_safe::gpu::Protected::No,
                    };

                    let backend_render_target = BackendRenderTarget::new_gl(
                        (size.width as i32, size.height as i32),
                        None,
                        8,
                        fb_info,
                    );

                    let mut gpu_surface = Surface::from_backend_render_target(
                        gr_context,
                        &backend_render_target,
                        SurfaceOrigin::BottomLeft,
                        ColorType::RGBA8888,
                        None,
                        None,
                    )
                    .expect("Failed to create GPU surface");

                    let canvas = gpu_surface.canvas();
                    canvas.clear(Color::DARK_GRAY);

                    for (_id, app_win) in &mut self.app_windows {
                        // Raster -> GPU Texture
                        let image = app_win.surface.image_snapshot();
                        canvas.draw_image(
                            &image,
                            (app_win.rect.x(), app_win.rect.y()),
                            Some(&Paint::default()),
                        );

                        let mut p = Paint::default();
                        p.set_style(skia_safe::paint::Style::Stroke);
                        if Some(_id) == self.dragging_window.as_ref() {
                            p.set_color(Color::GREEN);
                            p.set_stroke_width(3.0);
                        } else {
                            p.set_color(Color::WHITE);
                            p.set_stroke_width(1.0);
                        }
                        canvas.draw_rect(app_win.rect, &p);
                    }

                    // 3. Swap Buffers
                    gr_context.flush_and_submit();
                    gl_surface.swap_buffers(gl_context).unwrap();

                    window.request_redraw();
                }
            }

            WindowEvent::CursorMoved { position, .. } => {
                self.cursor_position = (position.x, position.y);
                if let Some(app_id) = &self.dragging_window {
                    if let Some(win) = self.app_windows.get_mut(app_id) {
                        let new_x = position.x as f32 - self.drag_offset.0;
                        let new_y = position.y as f32 - self.drag_offset.1;
                        win.rect =
                            Rect::from_xywh(new_x, new_y, win.rect.width(), win.rect.height());
                        if let Some(w) = &self.window {
                            w.request_redraw();
                        }
                    }
                }
            }
            WindowEvent::MouseInput { state, button, .. } => {
                use winit::event::{ElementState, MouseButton};
                if button == MouseButton::Left {
                    if state == ElementState::Pressed {
                        let mx = self.cursor_position.0 as f32;
                        let my = self.cursor_position.1 as f32;
                        for (id, win) in &self.app_windows {
                            if mx >= win.rect.left()
                                && mx <= win.rect.right()
                                && my >= win.rect.top()
                                && my <= win.rect.bottom()
                            {
                                self.dragging_window = Some(id.clone());
                                self.drag_offset = (mx - win.rect.left(), my - win.rect.top());
                                break;
                            }
                        }
                    } else {
                        self.dragging_window = None;
                    }
                }
            }
            WindowEvent::Touch(touch) => match touch.phase {
                TouchPhase::Started => {
                    let mx = touch.location.x as f32;
                    let my = touch.location.y as f32;
                    for (id, win) in &self.app_windows {
                        if mx >= win.rect.left()
                            && mx <= win.rect.right()
                            && my >= win.rect.top()
                            && my <= win.rect.bottom()
                        {
                            self.dragging_window = Some(id.clone());
                            self.drag_offset = (mx - win.rect.left(), my - win.rect.top());
                            break;
                        }
                    }
                }
                TouchPhase::Ended => {
                    self.dragging_window = None;
                }
                TouchPhase::Cancelled => {
                    self.dragging_window = None;
                }
                TouchPhase::Moved => {
                    let mx = touch.location.x;
                    let my = touch.location.y;
                    self.cursor_position = (mx, my);
                    if let Some(app_id) = &self.dragging_window {
                        if let Some(win) = self.app_windows.get_mut(app_id) {
                            let new_x = mx as f32 - self.drag_offset.0;
                            let new_y = my as f32 - self.drag_offset.1;
                            win.rect =
                                Rect::from_xywh(new_x, new_y, win.rect.width(), win.rect.height());
                            if let Some(w) = &self.window {
                                w.request_redraw();
                            }
                        }
                    }
                }
            },
            _ => (),
        }
    }
}

pub fn run() {
    let event_loop = EventLoop::<VirtelEvent>::with_user_event().build().unwrap();
    event_loop.set_control_flow(ControlFlow::Poll);
    let proxy = event_loop.create_proxy();
    let mut app = VirtelOS::new(proxy);
    event_loop.run_app(&mut app).unwrap();
}
