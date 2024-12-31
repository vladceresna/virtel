use natural_tts::{models::gtts::GttsModel, Model, NaturalTtsBuilder};



uniffi::include_scaffolding!("math");


fn add(a: u32, b: u32) -> u32 {
    a + b
}

fn tts_say(text: String) {
    let mut natural = NaturalTtsBuilder::default()
        .gtts_model(GttsModel::default())
        .default_model(Model::Gtts)
        .build().unwrap();

    let _ = natural.say_auto(text).unwrap();
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