use mio::net::{TcpListener, TcpStream};
use mio::{Events, Interest, Poll, Token};
use native_tls::{TlsConnector, TlsStream};
use std::io::{Read, Write};
use std::net::SocketAddr;

const SERVER: Token = Token(0);

pub fn start_server(addr: SocketAddr) -> std::io::Result<()> {
    let mut listener = TcpListener::bind(addr)?;
    let mut poll = Poll::new()?;
    let mut events = Events::with_capacity(128);

    poll.registry().register(&mut listener, SERVER, Interest::READABLE)?;

    let mut connections = Vec::new();
    let mut unique_token = 1;

    loop {
        poll.poll(&mut events, None)?;

        for event in events.iter() {
            match event.token() {
                SERVER => {
                    if let Ok((mut stream, _)) = listener.accept() {
                        let token = Token(unique_token);
                        unique_token += 1;

                        poll.registry().register(&mut stream, token, Interest::READABLE | Interest::WRITABLE)?;
                        connections.push((token, stream));
                    }
                }
                token => {
                    if let Some((_, stream)) = connections.iter_mut().find(|(t, _)| *t == token) {
                        if event.is_readable() {
                            let mut buf = [0; 1024];
                            match stream.read(&mut buf) {
                                Ok(0) => {
                                    // Connection closed
                                    poll.registry().deregister(stream)?;
                                    connections.retain(|(t, _)| *t != token);
                                }
                                Ok(n) => {
                                    println!("Received: {}", String::from_utf8_lossy(&buf[..n]));
                                }
                                Err(e) => {
                                    eprintln!("Failed to read: {}", e);
                                }
                            }
                        }

                        if event.is_writable() {
                            if let Err(e) = stream.write_all(b"Hello from server!") {
                                eprintln!("Failed to write: {}", e);
                            }
                        }
                    }
                }
            }
        }
    }
}

pub fn start_tcp_client(addr: SocketAddr) -> std::io::Result<()> {
    let mut stream = TcpStream::connect(addr)?;
    stream.write_all(b"Hello from client!")?;

    let mut buf = [0; 1024];
    let n = stream.read(&mut buf)?;
    println!("Client received: {}", String::from_utf8_lossy(&buf[..n]));

    Ok(())
}

pub fn start_https_client(addr: &str) -> std::io::Result<()> {
    let connector = TlsConnector::new().unwrap();
    let stream = std::net::TcpStream::connect(addr)?;
    let mut tls_stream = connector.connect(addr, stream).unwrap();

    tls_stream.write_all(b"GET / HTTP/1.0\r\n\r\n")?;

    let mut buf = Vec::new();
    tls_stream.read_to_end(&mut buf)?;
    println!("HTTPS Client received: {}", String::from_utf8_lossy(&buf));

    Ok(())
}