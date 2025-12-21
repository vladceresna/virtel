// use minreq::get;
// use percent_encoding::{utf8_percent_encode, AsciiSet, CONTROLS};
// use std::fs::File;
// use std::io::prelude::*;

// const FRAGMENT: &AsciiSet = &CONTROLS.add(b' ').add(b'"').add(b'<').add(b'>').add(b'`');

// /// language = en, ru, etc.
// pub fn save_to_file(text: &str, language: &str, filename: &str) -> bool {
//     let len = text.len();
//     let text = utf8_percent_encode(text, FRAGMENT).to_string();

//     if let Ok(rep) = get(format!("https://translate.google.com/translate_tts?ie=UTF-8&q={}&tl={}&total=1&idx=0&textlen={}&tl=en&client=tw-ob", text, language, len)).send() {
//         if let Ok(mut file) = File::create(filename) {
//             let bytes = rep.as_bytes();
//             if bytes.len() > 0 {
//                 if file.write_all(bytes).is_ok() {
//                     return true;
//                 }
//             }
//         }
//     }
//     false
// }

// #[cfg(test)]
// mod tests {
//     use super::*;

//     #[test]
//     fn test() {
//         assert!(save_to_file("Hello world!", "en", "test.mp3"));
//     }
// }

// use crate::crypto::*;

// use gtts::save_to_file;
// use rodio::{source::Source, Decoder, OutputStream};
// use std::{fs::File, io::BufReader};
// use whichlang::detect_language;

// fn tts_say_lang(text: String, path: String, language: String) {
//     save_to_file(&text, &language, &path);
//     play_mp3(path);
// }

// fn tts_say(text: String, path: String) {
//     tts_say_lang(text.clone(), path, detect_lang_whichlang_code(text));
// }

// fn play_mp3(path: String) {
//     let (_stream, stream_handle) = OutputStream::try_default().unwrap();
//     let file = BufReader::new(File::open(path.as_str()).unwrap());
//     let source = Decoder::new(file).unwrap();
//     let _ = stream_handle.play_raw(source.convert_samples());
//     std::thread::sleep(std::time::Duration::from_secs(5));
// }
// fn detect_lang_whichlang_code(text: String) -> String {
//     let mut language = detect_language(&text).three_letter_code().to_string();
//     language.pop();
//     language
// }

// #[cfg(test)]
// mod tests {
//     use super::*;

//     #[test]
//     fn it_works() {
//         tts_say("Вітаю".to_string(), "voicemessage.mp3".to_string());
//         assert_eq!(4, 4);
//     }
// }
