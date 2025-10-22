use once_cell::sync::Lazy;
use tokio::runtime::{Builder, Runtime};
use std::sync::Arc;

static TOKIO: Lazy<Arc<Runtime>> = Lazy::new(|| {
    Arc::new(
        Builder::new_multi_thread()
            .worker_threads(4)
            .enable_all()
            .build()
            .expect("Failed to start Tokio runtime"),
    )
});

pub fn spawn_task<F>(future: F)
where
    F: std::future::Future<Output = ()> + Send + 'static,
{
    TOKIO.spawn(future);
}

pub fn block_on<F: std::future::Future<Output = T> + Send, T>(future: F) -> T {
    TOKIO.block_on(future)
}