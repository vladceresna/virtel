use std::sync::{Arc, Mutex, MutexGuard};

use once_cell::sync::Lazy;

use crate::log;

#[cfg(any(target_os = "windows", target_os = "linux", target_os = "macos"))]
fn logAtOs(content: &str) {
    println!("{}", content);
}
#[cfg(target_os = "android")]
fn logAtOs(content: &str) {
    println!("{}", content);
}
#[cfg(target_os = "ios")]
fn logAtOs(content: &str) {
    println!("{}", content);
}

pub fn log(log_type: Log, content: &str) {
    let log_type = match log_type {
        Log::Error => "ERRR",
        Log::Info => "INFO",
        Log::Success => "SCSS",
        Log::Warning => "WRNG",
    };
    logStr(format!("[{}] {}", log_type, content).as_str());
}
#[uniffi::export]
pub fn logStr(content: &str) {
    logAtOs(content);
    pushToLog(content);
}
#[uniffi::export]
pub fn snapshotLog() -> Vec<String> {
    getLogger().lock().expect("Error Logger locking").clone()
}
fn pushToLog(content: &str) {
    getLogger()
        .lock()
        .expect("Error Logger locking")
        .push(content.to_string());
}
fn getLogger() -> Arc<Mutex<Vec<String>>> {
    Arc::clone(&LOGGER)
}

static LOGGER: Lazy<Arc<Mutex<Vec<String>>>> = Lazy::new(|| Arc::new(Mutex::new(Vec::new())));

pub enum Log {
    Error,
    Info,
    Warning,
    Success,
}
