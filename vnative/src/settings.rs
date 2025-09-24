
pub struct Settings {
    filesystem: FileSystem,
}

pub struct FileSystem {
    os_home_dir: String,
    virtel_dir: String,
    apps_dir: String,
    settings_file: String
} impl FileSystem {
    // pub fn new() -> Self {
    //     let home_dir = dirs::home_dir().unwrap_or_else(|| std::path::PathBuf::from("."));
    //     let home_str = home_dir.to_str().unwrap_or(".").to_string();
    //     let virtel_dir = format!("{}/.virtel", home_str);
    //     let apps_dir = format!("{}/apps", virtel_dir);
    //     let settings_file = format!("{}/settings.json", virtel_dir);

    //     // Create directories if they don't exist
    //     std::fs::create_dir_all(&apps_dir).unwrap_or_else(|e| {
    //         eprintln!("Failed to create apps directory: {}", e);
    //     });

    //     Self {
    //         os_home_dir: home_str,
    //         virtel_dir,
    //         apps_dir,
    //         settings_file
    //     }
    // }

    // pub fn get_os_home_dir(&self) -> &str {
    //     &self.os_home_dir
    // }

    // pub fn get_virtel_dir(&self) -> &str {
    //     &self.virtel_dir
    // }

    // pub fn get_apps_dir(&self) -> &str {
    //     &self.apps_dir
    // }

    // pub fn get_settings_file(&self) -> &str {
    //     &self.settings_file
    // }
}

pub fn getOsHomeDir() -> String {
    "".to_string()
}