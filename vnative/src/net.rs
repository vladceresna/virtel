use crate::tokio_setup::TOKIO;
use reqwest::{header::HeaderMap, Client, Method};

pub fn fetch(
    url: String,
    method: Method, // GET, POST и т.д.
    headers: Option<HeaderMap>,
    body: Option<String>,
) -> Result<(HeaderMap, String), reqwest::Error> {
    let runtime = TOKIO.clone();
    runtime.block_on(async move {
        let client = Client::new();
        let mut request = client.request(method, &url);

        if let Some(h) = headers {
            request = request.headers(h);
        }

        if let Some(b) = body {
            request = request.body(b);
        }

        let resp = request.send().await?;
        let resp_headers = resp.headers().clone();
        let resp_body = resp.text().await?;
        Ok((resp_headers, resp_body))
    })
}
