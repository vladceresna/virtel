use winit::{
    event::{Event, WindowEvent},
    event_loop::{ControlFlow, EventLoop},
    window::WindowBuilder,
};

fn main() {
    let event_loop = EventLoop::new().unwrap();
    let window = WindowBuilder::new()
        .with_title("Virtel Native — чистый Rust")
        .with_inner_size(winit::dpi::LogicalSize::new(1000, 700))
        .build(&event_loop)
        .unwrap();

    // Пока просто белое окно — дальше будем рисовать Skia
    println!("Virtel запущен на чистом Rust! Окно открыто.");

    event_loop
        .run(move |event, _, control_flow| {
            *control_flow = ControlFlow::Wait;

            match event {
                Event::WindowEvent {
                    event: WindowEvent::CloseRequested,
                    ..
                } => {
                    println!("Закрываем приложение...");
                    *control_flow = ControlFlow::Exit;
                }
                Event::WindowEvent {
                    event: WindowEvent::Resized(size),
                    ..
                } => {
                    println!("Размер окна: {}x{}", size.width, size.height);
                }
                _ => (),
            }
        })
        .unwrap();
}
