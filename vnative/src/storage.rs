use std::{
    collections::HashMap,
    fs,
    path::Path,
    sync::{atomic::{AtomicBool, Ordering}, mpsc, Arc, Mutex},
    thread,
    time::Duration,
};

pub struct Storage {
    pub data: Arc<Mutex<HashMap<String, String>>>,
    pub path: String,
    pub tx: mpsc::Sender<i8>,
    pub working: Arc<Mutex<bool>>
} impl Storage {
    pub fn new(path: String) -> Self {
        let (tx, rx) = mpsc::channel();
        let initial_data = if Path::new(&path).exists() {
            let content = fs::read_to_string(&path)
                .expect("Reading is not done");
            serde_json::from_str(&content).unwrap_or_else(|_| HashMap::new())
        } else {
            HashMap::new()
        };

        let storage = Storage {
            data: Arc::new(Mutex::new(initial_data)),
            path: path.clone(),
            tx, working: Arc::new(Mutex::new(true))
        };

        storage.start_follow(rx);

        storage
    }

    fn start_follow(&self, rx: mpsc::Receiver<i8>) {
        let data = Arc::clone(&self.data);
        let path = self.path.clone();
        let working = Arc::clone(&self.working);

        thread::spawn(move || loop {
            
            if let Ok(_msg) = rx.try_recv() {
                let data_guard = data.lock().unwrap();
                let json = serde_json::to_string(&*data_guard)
                    .expect("Serialization failed");
                let mut childPath: &Path = Path::new(&path);
                fs::create_dir_all(childPath.parent().unwrap());
                if fs::exists(&path).unwrap() {
                    fs::remove_file(&path).unwrap()
                }
                fs::write(&path, json).expect("File writing failed");
            }
            {if !*(working.clone().lock().unwrap()) {
                break;
            }}
            thread::sleep(Duration::from_millis(10));
        });
    }
    pub fn stop(&self){
        *self.working.lock().unwrap() = false;
        thread::sleep(Duration::from_millis(30));
    }

    pub fn set(&self, name: String, value: String) {
        let mut data = self.data.lock().unwrap();
        data.insert(name, value);
        self.tx.send(0).expect("Sending message in channel failed");
    }

    pub fn get(&self, name: String) -> String {
        let data = self.data.lock().unwrap();
        return match data.get(&name).cloned() {
            Some(value) => value,
            None => "".to_string(),
        };
    }

    pub fn del(&self, name: String) {
        let mut data = self.data.lock().unwrap();
        data.remove(&name);
        self.tx.send(0).expect("Sending message in channel failed");
    }
}


#[cfg(test)]
mod tests {
    use std::{thread, time::{self, Duration, SystemTime}};
    use super::Storage;
    
    #[test]
    fn storage_works(){
        let mut stg = Storage::new("/home/vlad/Документи/Tests/test.json".to_string());
        stg.set("name".to_string(), "value".to_string());
        assert_eq!("value".to_string(),stg.get("name".to_string()));
        stg.set("name".to_string(), "valuee".to_string());
        assert_eq!("valuee".to_string(),stg.get("name".to_string()));
        stg.stop();
    }

    #[test]
    fn storage_set(){
        let mut stg = Storage::new("/home/vlad/Документи/Tests/test.json".to_string());

        stg.set("name".to_string(), "value".to_string());
        
        assert_ne!("valuee".to_string(),stg.get("name".to_string()));
        stg.stop();
    }

}