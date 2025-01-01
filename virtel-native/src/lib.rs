use natural_tts::{models::parler::ParlerModel, Model, NaturalTtsBuilder};




uniffi::include_scaffolding!("virtel-native");


fn add(a: u32, b: u32) -> u32 {
    a + b
}


use std::io;
use std::iter;

use io_bluetooth::bt::{self, BtStream};


fn blt_() {
    let devices = bt::discover_devices().unwrap();

    println!("Devices:");
    for (idx, device) in devices.iter().enumerate() {
        println!("{}: {}", idx, *device);
    }

    if devices.len() == 0 {
        {}
    }

    let device_idx = request_device_idx(devices.len()).unwrap();

    let socket = BtStream::connect(iter::once(&devices[device_idx]), bt::BtProtocol::RFCOMM).unwrap();

    match socket.peer_addr() {
        Ok(name) => println!("Peername: {}.", name.to_string()),
        Err(err) => println!("An error occured while retrieving the peername: {:?}", err),
    }

    match socket.local_addr() {
        Ok(name) => println!("Socket name: {}", name.to_string()),
        Err(err) => println!("An error occured while retrieving the sockname: {:?}", err),
    }

    let mut buffer = vec![0; 1024];
    loop {
        match socket.recv(&mut buffer[..]) {
            Ok(len) => println!("Received {} bytes.", len),
            Err(err) => {},
        }
    }
}

fn request_device_idx(len: usize) -> io::Result<usize> {
    println!("Please specify the index of the Bluetooth device you want to connect to:");

    let mut buffer = String::new();
    loop {
        io::stdin().read_line(&mut buffer)?;
        if let Ok(idx) = buffer.trim_end().parse::<usize>() {
            if idx < len {
                return Ok(idx);
            }
        }
        buffer.clear();
        println!("Invalid index. Please try again.");
    }
}







fn tts_say(text: String) {
    // let desc = "A female speaker in fast calming voice in a quiet environment".to_string();
    // let model = "parler-tts/parler-tts-mini-expresso".to_string();
    // let parler = ParlerModel::new(desc, model, false);

    // // Create the NaturalTts using the Builder pattern
    // let mut natural = NaturalTtsBuilder::default()
    //     .parler_model(parler.unwrap())
    //     .default_model(Model::Parler)
    //     .build().unwrap();

    // // Use the pre-included function to say a message using the default_model.
    // let _ = natural.say_auto(text).unwrap();
}




#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn it_blt(){
        blt_();
        assert_eq!(10,add(5, 5))
    }

    #[test]
    fn it_works() {
        tts_say("Hello".to_string());
        let result = add(2, 2);
        assert_eq!(result, 4);
    }
}