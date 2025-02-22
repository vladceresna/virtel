
uniffi::include_scaffolding!("vnative");

use crate::storage::Storage;
use gtts::save_to_file;
use whichlang::detect_language;
use std::{fs::File,io::BufReader};
use rodio::{Decoder, OutputStream, source::Source};


mod gtts;
mod storage;



fn add(a: u32, b: u32) -> u32 {
    a + b
}

fn tts_say_lang(text: String, path: String, language: String) {
    save_to_file(&text, &language,&path);
    play_mp3(path);
}


fn tts_say(text: String, path: String) {
    tts_say_lang(text.clone(), path, detect_lang_whichlang_code(text));
}

fn play_mp3(path: String){
    let (_stream, stream_handle) = OutputStream::try_default().unwrap();
    let file = BufReader::new(File::open(path.as_str()).unwrap());
    let source = Decoder::new(file).unwrap();
    let _ = stream_handle.play_raw(source.convert_samples());
    std::thread::sleep(std::time::Duration::from_secs(5));
}
fn detect_lang_whichlang_code(text: String)-> String{
     let mut language = detect_language(&text).three_letter_code().to_string();
     language.pop();
     language
 }




#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn it_works() {
        tts_say("Вітаю".to_string(),"voicemessage.mp3".to_string());
        let result = add(2, 2);
        assert_eq!(result, 4);
    }
}