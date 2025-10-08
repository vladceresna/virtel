use crate::center::get_virtel_center;

pub struct Settings {
    pub filesystem: FileSystem,
} impl Settings {
    pub fn new(filesystem: FileSystem) -> Self {
        Self { filesystem }
    }
}

pub struct FileSystem {
    pub os_home_dir: String,
    pub virtel_dir: String,
    pub apps_dir: String,
    pub settings_file: String,
}
impl FileSystem {
    pub fn new() -> Self {
        let home_str = get_virtel_center().get_system_api().get_os_home_dir().unwrap();
        let virtel_dir = format!("{}/.virtel", home_str);
        let apps_dir = format!("{}/apps", virtel_dir);
        let settings_file = format!("{}/settings.json", virtel_dir);

        // Create directories if they don't exist
        if std::path::Path::new(&apps_dir).exists() == false {
            std::fs::create_dir_all(&apps_dir).unwrap_or_else(|e| {
                eprintln!("Failed to create virtel directory: {}", e);
            });
        }

        Self {
            os_home_dir: home_str,
            virtel_dir,
            apps_dir,
            settings_file
        }
    }
}

