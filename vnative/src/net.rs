use crate::tokio_setup::spawn_task;

#[uniffi::export]
pub fn start_http_server(port: u16) {
    spawn_task(async move {
        use hyper::{service::{make_service_fn, service_fn}, Body, Request, Response, Server};

        let make_svc = make_service_fn(|_conn| async {
            Ok::<_, hyper::Error>(service_fn(|_req: Request<Body>| async {
                Ok::<_, hyper::Error>(Response::new(Body::from("Hello from Rust server!")))
            }))
        });

        let addr = ([0, 0, 0, 0], port).into();
        let server = Server::bind(&addr).serve(make_svc);

        if let Err(e) = server.await {
            eprintln!("server error: {}", e);
        }
    });
}
