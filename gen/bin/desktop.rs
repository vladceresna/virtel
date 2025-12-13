fn main() {
    #[cfg(not(any(target_os = "android", target_os = "ios")))]
    virtel::run();
}
