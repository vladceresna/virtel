use crate::center::get_virtel_center;
use ruwren::{foreign_v2::WrenString, wren_impl, wren_module, WrenObject};
use std::fs;

fn get_full_os_path(path: String) -> String {
    let virtel = get_virtel_center()
        .get_settings()
        .filesystem
        .virtel_dir
        .clone();
    format!("{}{}", virtel, path)
}

#[derive(WrenObject, Default)]
pub struct FS {}

#[wren_impl]
impl FS {
    fn readFile(&self, path: WrenString) -> String {
        let path = get_full_os_path(path.into_string().unwrap());
        fs::read_to_string(&path).unwrap_or_else(|_| String::new())
    }

    fn writeFile(&self, path: WrenString, content: WrenString) {
        let path_str = path.into_string().unwrap();
        let content_str = content.into_string().unwrap();
        let full_path = get_full_os_path(path_str);
        if let Some(parent) = std::path::Path::new(&full_path).parent() {
            let _ = fs::create_dir_all(parent);
        }
        if let Err(e) = fs::write(&full_path, content_str) {
            eprintln!("FS Error writing to {}: {}", full_path, e);
        }
    }

    fn existsPath(&self, path: WrenString) -> bool {
        let path = get_full_os_path(path.into_string().unwrap());
        std::path::Path::new(&path).exists()
    }

    fn createPath(&self, path: WrenString) {
        let path = get_full_os_path(path.into_string().unwrap());
        let _ = fs::create_dir_all(path);
    }

    fn deletePath(&self, path: WrenString) {
        let path = get_full_os_path(path.into_string().unwrap());
        let _ = fs::remove_dir_all(path);
    }
}

pub fn virtel_fs_api_wren_bindings() -> &'static str {
    return r#"
class FS {
    foreign static readFile(path)
    foreign static writeFile(path, content)
    foreign static existsPath(path)
    foreign static createPath(path)
    foreign static deletePath(path)
}
"#;
}

wren_module! {
    pub mod virtel_fs {
        pub crate::api::virtel_fs_api::FS;
    }
}
