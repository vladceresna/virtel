
uniffi::include_scaffolding!("vnative");

use gtts::save_to_file;
use whichlang::detect_language;
use std::{fs::File,io::BufReader};
use rodio::{Decoder, OutputStream, source::Source};
use lingua::{LanguageDetectorBuilder};



mod gtts;



fn add(a: u32, b: u32) -> u32 {
    a + b
}

fn tts_say_lang(text: String, language: String, path: String) {
    save_to_file(&text, &language,&path);
    play_mp3(path);
}

fn detect_lang_lingua_code(text: String)-> String{
    let detector = LanguageDetectorBuilder::from_all_languages().with_preloaded_language_models().build();
    let detected_language = detector.detect_language_of(text.clone());
    detected_language.unwrap().iso_code_639_1().to_string()
}

fn detect_lang_whichlang_code(text: String)-> String{
    let mut language = detect_language(&text).three_letter_code().to_string();
    language.pop();
    language
}

enum LangDetectEngine {
    WHICHLANG,
    LINGUA
}

fn tts_say(text: String, path: String, engine: LangDetectEngine) {
    match engine {
        LangDetectEngine::WHICHLANG => {
            tts_say_lang(text.clone(), detect_lang_whichlang_code(text), path);
        },
        LangDetectEngine::LINGUA => {
            tts_say_lang(text.clone(), detect_lang_lingua_code(text), path);
        }
    }
    
}

fn play_mp3(path: String){
    let (_stream, stream_handle) = OutputStream::try_default().unwrap();
    let file = BufReader::new(File::open(path.as_str()).unwrap());
    let source = Decoder::new(file).unwrap();
    let _ = stream_handle.play_raw(source.convert_samples());
    std::thread::sleep(std::time::Duration::from_secs(5));
}



#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn it_blt(){
        assert_eq!(10,add(5, 5))
    }

    #[test]
    fn it_works() {
        tts_say("Вітаю".to_string(),"voicemessage.mp3".to_string(),LangDetectEngine::WHICHLANG);
        tts_say("Вітаю".to_string(),"voicemessage.mp3".to_string(),LangDetectEngine::LINGUA);
        let result = add(2, 2);
        assert_eq!(result, 4);
    }
}