use std::fs;

use ruwren::{foreign_v2::WrenString, wren_impl, wren_module, WrenObject};

use crate::center::get_virtel_center;

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
    #[allow(non_snake_case)]
    fn readFile(&self, path: WrenString) -> String {
        let path = get_full_os_path(path.into_string().unwrap());
        fs::read_to_string(path).unwrap()
    }
    #[allow(non_snake_case)]
    fn writeFile(&self, path: WrenString, content: WrenString) {
        let path = get_full_os_path(path.into_string().unwrap());
        fs::write(get_full_os_path(path), content.into_string().unwrap()).unwrap();
    }
    #[allow(non_snake_case)]
    fn existsPath(&self, path: WrenString) -> bool {
        let path = get_full_os_path(path.into_string().unwrap());
        fs::exists(path).unwrap()
    }
    #[allow(non_snake_case)]
    fn createPath(&self, path: WrenString) {
        let path = get_full_os_path(path.into_string().unwrap());
        fs::create_dir_all(path).unwrap()
    }
    #[allow(non_snake_case)]
    fn deletePath(&self, path: WrenString) {
        let path = get_full_os_path(path.into_string().unwrap());
        fs::remove_dir_all(path).unwrap()
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
