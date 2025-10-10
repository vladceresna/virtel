use std::fs;
use std::io::{self, Read};
use std::path::Path;

use crate::center::get_virtel_center;

pub fn install_app(vc_file_path: &str) -> io::Result<()> {
    // Read the .vc file
    let vc_file_path = Path::new(vc_file_path);
    if !vc_file_path.exists() {
        return Err(io::Error::new(
            io::ErrorKind::NotFound,
            "The specified .vc file does not exist",
        ));
    }

    let mut vc_file = fs::File::open(vc_file_path)?;
    let mut bytecode = Vec::new();
    vc_file.read_to_end(&mut bytecode)?;


    let apps_dir = get_virtel_center().get_settings().filesystem.apps_dir.clone();

    // Write the bytecode to the Virtel filesystem
    let apps_path = Path::new(apps_dir.as_str());
    if !apps_path.exists() {
        fs::create_dir_all(apps_path)?;
    }

    let app_name = vc_file_path
        .file_stem()
        .ok_or_else(|| io::Error::new(io::ErrorKind::InvalidInput, "Invalid .vc file name"))?;
    let app_path = apps_path.join(app_name).join(app_name).with_extension("vc");

    fs::write(app_path, bytecode)?;

    println!("App successfully copied to Virtel filesystem.");
    Ok(())
}


pub fn remove_app(app_id: &str) -> io::Result<()> {
    let apps_dir = get_virtel_center().get_settings().filesystem.apps_dir.clone();
    let app_path = Path::new(apps_dir.as_str()).join(app_id);

    if app_path.exists() {
        fs::remove_dir_all(&app_path)?;
        println!("App '{}' successfully removed.", app_id);
        Ok(())
    } else {
        Err(io::Error::new(
            io::ErrorKind::NotFound,
            "The specified app does not exist",
        ))
    }
}