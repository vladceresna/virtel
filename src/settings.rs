use std::path::{Path, PathBuf};

// Удаляем зависимость от center, чтобы не было Deadlock
// use crate::center::get_virtel_center;

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
        // ИСПРАВЛЕНИЕ: Получаем HOME напрямую через std::env,
        // не обращаясь к get_virtel_center()
        let home_str = std::env::var("HOME").unwrap_or_else(|_| ".".to_string());

        // Для Android логика может быть другой, но пока чиним Десктоп
        // #[cfg(target_os = "android")]
        // let home_str = ...;

        let virtel_dir = format!("{}/.virtel/0", home_str);
        let virtel_home_dir: String = format!("{}/home", virtel_dir);
        let apps_dir = format!("{}/sys/apps", virtel_dir);
        let settings_file = format!("{}/sys/settings.json", virtel_dir);

        // Создаем папки
        let apps_path = Path::new(&apps_dir);
        if !apps_path.exists() {
            // Используем match или unwrap_or_else, чтобы не крэшить программу жестко
            if let Err(e) = std::fs::create_dir_all(&apps_dir) {
                eprintln!(
                    "CRITICAL: Failed to create virtel directory {}: {}",
                    apps_dir, e
                );
            } else {
                println!("FS: Created apps directory: {}", apps_dir);
            }
        }

        // Также желательно создать саму папку .virtel/0, если её нет
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
