use crate::tokio_setup::TOKIO;
use reqwest::{header::HeaderMap, Client, Method};

pub enum RequestBody {
    Text(String),
    Bytes(Vec<u8>),
    None,
}

pub enum ResponseBody {
    Text,
    Bytes,
}

pub enum FetchResult {
    Text(String),
    Bytes(Vec<u8>),
}

pub fn fetch(
    url: String,
    method: Method,
    headers: Option<HeaderMap>,
    body: RequestBody,
    response_type: ResponseBody,
) -> Result<(HeaderMap, FetchResult), reqwest::Error> {
    let runtime = TOKIO.clone();
    runtime.block_on(async move {
        let client = Client::new();
        let mut request = client.request(method, &url);

        if let Some(h) = headers {
            request = request.headers(h);
        }

        match body {
            RequestBody::Text(txt) => {
                request = request.body(txt);
            }
            RequestBody::Bytes(bytes) => {
                request = request.body(bytes);
            }
            RequestBody::None => {}
        }

        let resp = request.send().await?;
        let resp_headers = resp.headers().clone();

        let result = match response_type {
            ResponseBody::Text => {
                let text = resp.text().await?;
                FetchResult::Text(text)
            }
            ResponseBody::Bytes => {
                let bytes = resp.bytes().await?.to_vec();
                FetchResult::Bytes(bytes)
            }
        };

        Ok((resp_headers, result))
    })
}
