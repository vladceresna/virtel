use ruwren::{foreign_v2::WrenString, wren_impl, wren_module, WrenObject};

#[derive(WrenObject, Default)]
pub struct HttpClient {}
#[wren_impl]
impl HttpClient {
    #[wren_impl(constructor)]
    fn new(&mut self) -> Result<HttpClientInstance, String> {
        Ok(HttpClientInstance {})
    }
    #[wren_impl(instance)]
    fn get(&self, url: WrenString) {
        println!("Making GET request to: {}", url.into_string().unwrap());
    }
}
pub fn virtel_net_api_wren_bindings() -> &'static str {
    r#"
foreign class HttpClient {
    construct new() {}
    foreign get(url)
}
"#
}
wren_module! {
    pub mod virtel_net {
        pub crate::api::virtel_net_api::HttpClient;
    }
}
