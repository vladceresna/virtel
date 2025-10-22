uniffi::setup_scaffolding!();

//modules
mod app;
mod apps;
mod center;
mod net;
mod settings;
mod tokio_setup;
mod vx;

use crate::center::VirtelCenter;
//code

fn add(a: u32, b: u32) -> u32 {
    a + b
}

//tests
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn it_works() {
        let result = add(2, 2);
        assert_eq!(result, 4);
    }
}
