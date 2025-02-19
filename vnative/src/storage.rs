use std::{
    collections::HashMap,
    fs,
    path::Path,
    sync::{mpsc, Arc, Mutex},
    thread,
    time::Duration,
};

pub struct Storage {
    pub data: Arc<Mutex<HashMap<String, String>>>,
    pub path: String,
    pub tx: mpsc::Sender<i8>,
} impl Storage {
    pub fn new(path: String) -> Self {
        let (tx, rx) = mpsc::channel();
        let initial_data = if Path::new(&path).exists() {
            let content = fs::read_to_string(&path)
                .expect("Не удалось прочитать файл");
            serde_json::from_str(&content).unwrap_or_else(|_| HashMap::new())
        } else {
            HashMap::new()
        };

        let storage = Storage {
            data: Arc::new(Mutex::new(initial_data)),
            path: path.clone(),
            tx,
        };

        storage.start_follow(rx);

        storage
    }

    fn start_follow(&self, rx: mpsc::Receiver<i8>) {
        let data = Arc::clone(&self.data);
        let path = self.path.clone();

        thread::spawn(move || loop {
            if let Ok(_msg) = rx.try_recv() {
                let data_guard = data.lock().unwrap();
                let json = serde_json::to_string(&*data_guard)
                    .expect("Не удалось сериализовать данные");
                fs::write(&path, json).expect("Не удалось записать файл");
            }
            thread::sleep(Duration::from_millis(100));
        });
    }

    pub fn set(&self, name: String, value: String) {
        let mut data = self.data.lock().unwrap();
        data.insert(name, value);
        self.tx.send(0).expect("Не удалось отправить сообщение в канал");
    }

    pub fn get(&self, name: String) -> String {
        let data = self.data.lock().unwrap();
        data.get(&name).cloned().unwrap()
    }

    pub fn del(&self, name: String) {
        let mut data = self.data.lock().unwrap();
        data.remove(&name);
        self.tx.send(0).expect("Не удалось отправить сообщение в канал");
    }
}


#[cfg(test)]
mod tests {

    use std::{thread, time::Duration};

    use super::Storage;

    
    #[test]
    fn storage_works(){
        let mut stg = Storage::new("/home/vlad/Документи/Tests/test.txt".to_string());
        stg.set("name".to_string(), "value".to_string());
        thread::sleep(Duration::from_secs(1));
        assert_eq!("value".to_string(),stg.get("name".to_string()));
    }

}