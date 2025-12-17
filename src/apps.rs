use std::fs;
use std::io::{self, Read};
use std::path::Path;

use hyper::{HeaderMap, Method};

use crate::center::get_virtel_center;
use crate::net::{fetch, FetchResult, RequestBody, ResponseBody};

pub fn prepare_apps() {
    println!("prepare apps 1");
    if !is_app_installed("vladceresna.virtel.launcher") {
        println!("installed");
        let (_, launcher) = fetch(
            "https://virtel.netlify.app/std-apps/4.0.0/vladceresna.virtel.launcher/code/app.wren"
                .to_string(),
            Method::GET,
            None,
            RequestBody::None,
            ResponseBody::Text,
        )
        .unwrap();
        if let FetchResult::Text(content) = launcher {
            install_app("vladceresna.virtel.launcher", content).unwrap();
        }
    }
    println!("prepare apps 2");
}

pub fn is_app_installed(app_id: &str) -> bool {
    let apps_dir = get_virtel_center()
        .get_settings()
        .filesystem
        .apps_dir
        .clone();

    // Write the bytecode to the Virtel filesystem
    let temp = format!("{}/{}/code/app.wren", apps_dir, app_id);
    let this_app_code_path = Path::new(&temp);
    return this_app_code_path.exists();
}

/// .vc - virtel bytecode
/// .vs - virtel scriptcode (bytecode text interpretation)
pub fn install_app(app_id: &str, content: String) -> io::Result<()> {
    let apps_dir = get_virtel_center()
        .get_settings()
        .filesystem
        .apps_dir
        .clone();

    // Write the bytecode to the Virtel filesystem
    let temp = format!("{}/{}/code", apps_dir, app_id);
    let this_app_code_path = Path::new(&temp);
    if !this_app_code_path.exists() {
        fs::create_dir_all(&this_app_code_path)?;
    }
    let app_bytecode_path = this_app_code_path.join("app.wren");
    fs::write(app_bytecode_path, content)?;

    println!("App isn`t successfully copied to Virtel filesystem.");
    Ok(())
}

pub fn remove_app(app_id: &str) -> io::Result<()> {
    let apps_dir = get_virtel_center()
        .get_settings()
        .filesystem
        .apps_dir
        .clone();
    let this_app_path = Path::new(apps_dir.as_str()).join(app_id);

    if this_app_path.exists() {
        fs::remove_dir_all(&this_app_path)?;
        println!("App '{}' successfully removed.", app_id);
        Ok(())
    } else {
        Err(io::Error::new(
            io::ErrorKind::NotFound,
            "The specified app does not exist",
        ))
    }
}
