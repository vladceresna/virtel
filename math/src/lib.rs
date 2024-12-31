use tts_rust::{languages::Languages, tts::GTTSClient};

uniffi::include_scaffolding!("math");


fn add(a: u32, b: u32) -> u32 {
    a + b
}

fn tts_say(text: String) {
    let mut narrator: GTTSClient = GTTSClient {
        volume: 1.0, 
        language: Languages::English, // use the Languages enum
        tld: "com",
    };
    narrator.speak(text.as_str());
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn it_works() {
        tts_say("Hello".to_string());
        let result = add(2, 2);
        assert_eq!(result, 4);
    }
}