use std::sync::{Arc, Mutex};

use once_cell::sync::Lazy;

#[cfg(any(target_os = "windows", target_os = "linux", target_os = "macos"))]
fn log_at_os(content: &str) {
    println!("{}", content);
}
#[cfg(target_os = "android")]
fn log_at_os(content: &str) {
    println!("{}", content);
}
#[cfg(target_os = "ios")]
fn log_at_os(content: &str) {
    println!("{}", content);
}

pub fn log(log_type: Log, content: &str) {
    let log_type = match log_type {
        Log::Error => "ERRR",
        Log::Info => "INFO",
        Log::Success => "SCSS",
        Log::Warning => "WRNG",
    };
    log_str(format!("[{}] {}", log_type, content).as_str());
}
#[uniffi::export]
pub fn log_str(content: &str) {
    log_at_os(content);
    push_to_log(content);
}
#[uniffi::export]
pub fn snapshot_log() -> Vec<String> {
    get_logger().lock().expect("Error Logger locking").clone()
}
fn push_to_log(content: &str) {
    get_logger()
        .lock()
        .expect("Error Logger locking")
        .push(content.to_string());
}
fn get_logger() -> Arc<Mutex<Vec<String>>> {
    Arc::clone(&LOGGER)
}

static LOGGER: Lazy<Arc<Mutex<Vec<String>>>> = Lazy::new(|| Arc::new(Mutex::new(Vec::new())));

pub enum Log {
    Error,
    Info,
    Warning,
    Success,
}
