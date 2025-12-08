use hyper::Method;
use ruwren::{foreign_v2::WrenString, wren_impl, wren_module, WrenObject};

use crate::net::{fetch, FetchResult, RequestBody, ResponseBody};

#[derive(WrenObject, Default)]
pub struct HttpClient {}
#[wren_impl]
impl HttpClient {
    fn get(&self, url: WrenString) -> String {
        let url = url.into_string().unwrap();
        match fetch(
            url,
            Method::GET,
            None,
            RequestBody::None,
            ResponseBody::Text,
        )
        .unwrap()
        .1
        {
            FetchResult::Text(t) => t,
            _ => panic!("It is bytes"),
        }
    }
}
pub fn virtel_net_api_wren_bindings() -> &'static str {
    r#"
class HttpClient {
    foreign static get(url)
}
"#
}
wren_module! {
    pub mod virtel_net {
        pub crate::api::virtel_net_api::HttpClient;
    }
}
