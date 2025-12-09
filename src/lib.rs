use std::collections::HashMap;
use std::io::Write;
use std::num::NonZeroU32;
use std::rc::Rc;
use std::sync::Arc;

use skia_safe::{Color, Paint, Rect, Surface};
use winit::application::ApplicationHandler;
use winit::event::WindowEvent;
use winit::event_loop::{ActiveEventLoop, ControlFlow, EventLoop, EventLoopProxy};
use winit::window::{Window, WindowId};

mod api;
mod app;
mod apps;
mod center;
mod events;
mod fonts;
mod log;
mod net;
mod settings;
mod tokio_setup;
mod ui_bridge;

use crate::center::{get_virtel_center, SystemApi, VirtelError};
use events::VirtelEvent;
use fonts::FontEngine;
use ui_bridge::WinitUiBridge;

fn log_now(msg: &str) {
    println!("{}", msg);
    std::io::stdout().flush().unwrap();
}

struct VirtualAppWindow {
    rect: Rect,
    surface: Surface,
    #[allow(dead_code)]
    title: String,
}

struct VirtelOS {
    window: Option<Rc<Window>>,
    softbuffer_surface: Option<softbuffer::Surface<Rc<Window>, Rc<Window>>>,
    screen_surface: Option<Surface>,
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
            softbuffer_surface: None,
            screen_surface: None,
            app_windows: HashMap::new(),
            core_initialized: false,
            proxy,
            font_engine: None,
            cursor_position: (0.0, 0.0),
            dragging_window: None,
            drag_offset: (0.0, 0.0),
        }
    }

    fn recreate_skia_surface(&mut self, width: i32, height: i32) {
        self.screen_surface = Some(
            skia_safe::surfaces::raster_n32_premul((width, height))
                .expect("Failed to create skia surface"),
        );
    }
}

impl ApplicationHandler<VirtelEvent> for VirtelOS {
    fn resumed(&mut self, event_loop: &ActiveEventLoop) {
        if self.window.is_none() {
            log_now("OS: App Resumed -> Creating Window");

            let win_attr = Window::default_attributes()
                .with_title("Virtel OS")
                .with_inner_size(winit::dpi::LogicalSize::new(800.0, 600.0))
                .with_visible(true)
                .with_decorations(false);

            let window = Rc::new(event_loop.create_window(win_attr).unwrap());
            let context = softbuffer::Context::new(window.clone()).unwrap();
            let surface = softbuffer::Surface::new(&context, window.clone()).unwrap();

            self.window = Some(window.clone());
            self.softbuffer_surface = Some(surface);
            self.font_engine = Some(FontEngine::new());

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
                        log_now("CORE: Thread started.");
                        let center = get_virtel_center();
                        let result = std::panic::catch_unwind(move || {
                            center.initialize(ui_api, sys_api);
                            log_now("CORE: Initialize SUCCESS.");
                        });
                        if let Err(err) = result {
                            log_now("!!! CORE THREAD PANICKED !!!");
                            if let Some(msg) = err.downcast_ref::<&str>() {
                                println!("Panic Error: {}", msg);
                            }
                        }
                    })
                    .expect("Failed to spawn core thread");

                self.core_initialized = true;
            }
            self.window.as_ref().unwrap().request_redraw();
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

                let mut surface =
                    &mut skia_safe::surfaces::raster_n32_premul((width as i32, height as i32))
                        .expect("Failed to create app surface");

                {
                    let mut canvas = surface.canvas();

                    canvas.clear(Color::WHITE);

                    // Header
                    let mut header_paint = Paint::default();
                    header_paint.set_color(Color::LIGHT_GRAY);
                    canvas.draw_rect(Rect::from_xywh(0.0, 0.0, width as f32, 30.0), &header_paint);

                    // Text
                    if let Some(fonts) = &self.font_engine {
                        fonts.draw_text(surface, &title, 10.0, 5.0, Color::BLACK, 20.0);
                        fonts.draw_text(
                            surface,
                            "Hello from Wren!",
                            10.0,
                            50.0,
                            Color::DARK_GRAY,
                            16.0,
                        );
                    }
                }

                let app_win = VirtualAppWindow {
                    rect: Rect::from_xywh(50.0, 50.0, width as f32, height as f32),
                    surface: surface.clone(),
                    title,
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
                    paint.set_color(Color::RED);
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

                        let mut found = None;
                        for (id, win) in &self.app_windows {
                            if mx >= win.rect.left()
                                && mx <= win.rect.right()
                                && my >= win.rect.top()
                                && my <= win.rect.bottom()
                            {
                                found = Some(id.clone());
                            }
                        }

                        if let Some(id) = found {
                            self.dragging_window = Some(id.clone());
                            let win = self.app_windows.get(&id).unwrap();
                            self.drag_offset = (mx - win.rect.left(), my - win.rect.top());
                        }
                    } else {
                        self.dragging_window = None;
                    }
                }
            }

            WindowEvent::KeyboardInput {
                event:
                    winit::event::KeyEvent {
                        state: winit::event::ElementState::Pressed,
                        physical_key:
                            winit::keyboard::PhysicalKey::Code(winit::keyboard::KeyCode::Escape),
                        ..
                    },
                ..
            } => {
                event_loop.exit();
            }

            WindowEvent::Resized(_) => {
                if let Some(w) = &self.window {
                    w.request_redraw();
                }
            }

            WindowEvent::RedrawRequested => {
                let window = if let Some(w) = &self.window {
                    w.clone()
                } else {
                    return;
                };
                let size = window.inner_size();
                if size.width == 0 || size.height == 0 {
                    return;
                }

                if let Some(sb_surface) = &mut self.softbuffer_surface {
                    let _ = sb_surface.resize(
                        NonZeroU32::new(size.width).unwrap(),
                        NonZeroU32::new(size.height).unwrap(),
                    );
                }

                let need_recreate = if let Some(s) = &self.screen_surface {
                    s.width() != size.width as i32 || s.height() != size.height as i32
                } else {
                    true
                };
                if need_recreate {
                    self.recreate_skia_surface(size.width as i32, size.height as i32);
                }

                if let (Some(sb_surface), Some(screen_surface)) =
                    (&mut self.softbuffer_surface, &mut self.screen_surface)
                {
                    let info = screen_surface.image_info();
                    {
                        let mut s = screen_surface.clone();
                        let canvas = s.canvas();

                        canvas.clear(Color::DARK_GRAY);

                        for (_id, app_win) in &mut self.app_windows {
                            let mut app_s = app_win.surface.clone();
                            let image = app_s.image_snapshot();

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
                    }
                    let mut buffer = sb_surface.buffer_mut().unwrap();
                    let peek = screen_surface.peek_pixels().unwrap();
                    let pixels: &[u32] = unsafe {
                        std::slice::from_raw_parts(
                            peek.addr() as *const u32,
                            (info.width() * info.height()) as usize,
                        )
                    };
                    buffer.copy_from_slice(pixels);
                    let _ = buffer.present();
                }
                window.request_redraw();
            }
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
