use once_cell::sync::Lazy;
use std::sync::Arc;
use tokio::runtime::{Builder, Runtime};

pub static TOKIO: Lazy<Arc<Runtime>> = Lazy::new(|| {
    Arc::new(
        Builder::new_multi_thread()
            .worker_threads(4)
            .enable_all()
            .build()
            .expect("Failed to start Tokio runtime"),
    )
});
