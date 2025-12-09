use std::path::{Path, PathBuf};

pub struct Settings {
    pub filesystem: FileSystem,
}

impl Settings {
    pub fn new(filesystem: FileSystem) -> Self {
        Self { filesystem }
    }
}

pub struct FileSystem {
    pub os_home_dir: String,
    pub virtel_dir: String,
    pub apps_dir: String,
    pub settings_file: String,
    pub virtel_home_dir: String,
}

impl FileSystem {
    pub fn new() -> Self {
        let home_str = std::env::var("HOME").unwrap_or_else(|_| ".".to_string());

        // #[cfg(target_os = "android")]
        // let home_str = ...;

        let virtel_dir = format!("{}/.virtel/0", home_str);
        let virtel_home_dir: String = format!("{}/home", virtel_dir);
        let apps_dir = format!("{}/sys/apps", virtel_dir);
        let settings_file = format!("{}/sys/settings.json", virtel_dir);

        let apps_path = Path::new(&apps_dir);
        if !apps_path.exists() {
            //unwrap_or_else
            if let Err(e) = std::fs::create_dir_all(&apps_dir) {
                eprintln!(
                    "CRITICAL: Failed to create virtel directory {}: {}",
                    apps_dir, e
                );
            } else {
                println!("FS: Created apps directory: {}", apps_dir);
            }
        }

        // .virtel/0
        if !Path::new(&virtel_dir).exists() {
            let _ = std::fs::create_dir_all(&virtel_dir);
        }

        Self {
            os_home_dir: home_str,
            virtel_dir,
            apps_dir,
            settings_file,
            virtel_home_dir,
        }
    }
}
