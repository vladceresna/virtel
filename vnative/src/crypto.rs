//crt


use std::fmt::format;

use base64::prelude::*;
use crystals_dilithium::dilithium3::{Keypair, PublicKey};
use sha3::{Digest, Sha3_256, Sha3_512};
use aes_gcm::{Aes256Gcm, Key, Nonce, aead::{Aead, KeyInit, AeadCore}};
use hex::{decode, encode};
use rand::rngs::OsRng;



/// public, private
fn kem_pair() -> Vec<String> {
    let mut rng = rand::thread_rng();
    let keys_bob = pqc_kyber::keypair(&mut rng).unwrap();
    Vec::from([hex::encode(keys_bob.public), hex::encode(keys_bob.secret)])
}
/// ciphertext, shared_secret
fn kem_encapsulate_public(public_key: String) -> Vec<String> {
    let mut rng = rand::thread_rng();
    let (ciphertext, shared_secret_alice) = pqc_kyber::encapsulate(
        &hex::decode(public_key).unwrap(), &mut rng).unwrap();
    Vec::from([hex::encode(ciphertext),hex::encode(shared_secret_alice)])
}
fn kem_decapsulate_private(ciphertext: String, secret_key: String) -> String {
    let shared_secret_bob = pqc_kyber::decapsulate(&hex::decode(ciphertext).unwrap(), &hex::decode(secret_key).unwrap()).unwrap();
    hex::encode(shared_secret_bob)
}

/// public, secret
fn dsa_pair() -> Vec<String> {
    let keys = Keypair::generate(None); // TODO: Some()
    Vec::from([hex::encode(keys.public.bytes), hex::encode(keys.secret.bytes)])
}
fn dsa_sign(msg: String, public_key: String, secret_key: String) -> String {
    let keys = keypair_from(public_key, secret_key);
    hex::encode(keys.sign(msg.as_bytes()))
}
fn dsa_verify(msg: String, sig: String, public_key: String) -> bool {
    let public = PublicKey {
        bytes: hex::decode(public_key).unwrap().try_into().unwrap()
    };
    public.verify(
       msg.as_bytes(),
       hex::decode(sig).unwrap().as_slice()
    )
}

fn base64_encode(open_text: String) -> String {
    BASE64_STANDARD.encode(open_text.as_bytes())
}
fn base64_decode(ciphertext: String) -> String {
    String::from_utf8(
        BASE64_STANDARD.decode(ciphertext.as_bytes()).unwrap()
    ).unwrap()
}


fn sha512_encrypt(open_text: String) -> String {
    let mut hasher = Sha3_512::new();
    hasher.update(open_text.as_bytes());
    hex::encode(hasher.finalize())
}
fn sha256_encrypt(open_text: String) -> String {
    let mut hasher = Sha3_256::new();
    hasher.update(open_text.as_bytes());
    hex::encode(hasher.finalize())
}

fn hmac_encrypt(open_text: String, code: String) -> String {
    sha512_encrypt(format!("{}<@hmac divider>{}",open_text,code))
}
fn hmac_verify(open_text: String, mac: String, code:String) -> bool {
    if hmac_encrypt(open_text, code).eq(&mac) {
        return true;
    }
    return false;
}



fn aes_encrypt(plaintext: String, key_str: String) -> String {
    let mut hasher = Sha3_256::new();
    hasher.update(key_str.as_bytes());
    let hashed_key_bytes = hasher.finalize();

    let key = Key::<Aes256Gcm>::from_slice(&hashed_key_bytes);
    let cipher = Aes256Gcm::new(key);

    let nonce = Aes256Gcm::generate_nonce(&mut OsRng);
    let ciphertext_bytes = cipher.encrypt(&nonce, plaintext.as_bytes()).unwrap();

    let mut combined = Vec::new();
    combined.extend_from_slice(nonce.as_slice());
    combined.extend_from_slice(&ciphertext_bytes);

    encode(combined)
}
fn aes_decrypt(encrypted_text: String, key_str: String) -> String {
    let mut hasher = Sha3_256::new();
    hasher.update(key_str.as_bytes());
    let hashed_key_bytes = hasher.finalize();

    let key = Key::<Aes256Gcm>::from_slice(&hashed_key_bytes);
    let cipher = Aes256Gcm::new(key);

    let decoded_bytes = decode(&encrypted_text).unwrap();
    let nonce = Nonce::from_slice(&decoded_bytes[..12]);
    let ciphertext_bytes = &decoded_bytes[12..];

    let plaintext_bytes = cipher.decrypt(nonce, ciphertext_bytes).unwrap();
    String::from_utf8(plaintext_bytes).unwrap()
}

fn jwt_encrypt(version: String, data: String, secret: String) -> String {
    format!(
        "{}.{}.{}",
        base64_encode(version.clone()),
        base64_encode(data.clone()),
        base64_encode(
            hmac_encrypt(
                format!(
                    "{}.{}",
                    base64_encode(version),
                    base64_encode(data)
                ),
                sha512_encrypt(secret)
            )
        )
    )
}
fn jwt_verify(vwt: String, secret: String) -> String {
    let vwt_arr = vwt.split('.').collect::<Vec<&str>>();
    let sig = base64_decode(vwt_arr.get(2).unwrap().to_string());
    let new_sig = hmac_encrypt(
        format!(
            "{}.{}",
            vwt_arr.get(0).unwrap().to_string(),
            vwt_arr.get(1).unwrap().to_string()
        ),
        sha512_encrypt(secret)
    );
    if sig.eq(&new_sig) {
        return base64_decode(vwt_arr.get(1).unwrap().to_string())
    } else {
        return "".to_string()
    }
}




fn keypair_from(public_key: String, secret_key: String) -> crystals_dilithium::dilithium3::Keypair {
    crystals_dilithium::dilithium3::Keypair {
        public: crystals_dilithium::dilithium3::PublicKey { 
            bytes: hex::decode(public_key).unwrap().try_into().unwrap()
        },
        secret: crystals_dilithium::dilithium3::SecretKey {
            bytes: hex::decode(secret_key).unwrap().try_into().unwrap()
        } 
    }
}


#[cfg(test)]
mod tests {


    use crate::crypto::{base64_decode, base64_encode, jwt_encrypt, jwt_verify};

    use super::{
        aes_decrypt, aes_encrypt, dsa_pair, dsa_sign, dsa_verify, hmac_encrypt, hmac_verify, kem_decapsulate_private, kem_encapsulate_public, kem_pair, sha512_encrypt
    };

    
    #[test]
    fn kem_works(){
        let public_private = kem_pair();
        let ciphertext_secret1 = kem_encapsulate_public(public_private.get(0).unwrap().to_string());
        let secret2 = kem_decapsulate_private(
            ciphertext_secret1.get(0).unwrap().to_string(), public_private.get(1).unwrap().to_string()
        );
        assert_eq!(
            ciphertext_secret1.get(1).unwrap().to_string(),
            secret2
        );
        println!("{}",secret2);
    }

    #[test]
    fn dsa_works(){
        let keys = dsa_pair();
        let message = "I am your friend Jake".to_string();
        let signature = dsa_sign(
            message.clone(), 
            keys.get(0).unwrap().to_string(),
            keys.get(1).unwrap().to_string()
        );
        
        let is_verified = dsa_verify(
            message,
            signature,
            keys.get(0).unwrap().to_string()
        );
        assert_eq!(
            is_verified,
            true
        );
        let keys = dsa_pair();
        let message = "I am your friend Jake".to_string();
        let signature = dsa_sign(
            message.clone(), 
            keys.get(0).unwrap().to_string(),
            keys.get(1).unwrap().to_string()
        );
        
        let is_verified = dsa_verify(
            "message".to_string(),
            signature,
            keys.get(0).unwrap().to_string()
        );
        assert_eq!(
            is_verified,
            false
        );
    }

    #[test]
    fn base64_works(){
        assert_eq!(base64_decode("aGVsbG8=".to_string()), "hello".to_string());
        assert_eq!(base64_encode("hello".to_string()), "aGVsbG8=".to_string());
    }

    #[test]
    fn sha512_works(){
        assert_eq!(
            sha512_encrypt("Hello".to_string()),
            sha512_encrypt("Hello".to_string())
        )
    }

    #[test]
    fn hmac_works(){
        assert_eq!(
            hmac_verify(
                "Hello".to_string(),
                hmac_encrypt(
                    "Hello".to_string(), 
                    sha512_encrypt("Hello_password".to_string())
                ),
                sha512_encrypt("Hello_password".to_string())
            ),
            true
        );
        assert_eq!(
            hmac_verify(
                "Hello".to_string(),
                hmac_encrypt(
                    "Hello".to_string(), 
                    sha512_encrypt("Hello_passwor".to_string())
                ),
                sha512_encrypt("Hello_password".to_string())
            ),
            false
        );
        assert_eq!(
            hmac_verify(
                "Hello".to_string(),
                hmac_encrypt(
                    "Hello".to_string(), 
                    sha512_encrypt("Hello_password".to_string())
                ),
                sha512_encrypt("Hello_passwor".to_string())
            ),
            false
        );
    }

    #[test]
    fn aes_works(){
        assert_eq!(
            aes_decrypt(
                aes_encrypt(
                    "hello".to_string(), 
                    "hello_code".to_string()
                ),
                "hello_code".to_string()
            ),
            "hello".to_string()
        );
    }

    #[test]
    fn jwt_works(){
        assert_eq!(
            jwt_verify(
                jwt_encrypt(
                    "0.1".to_string(), 
                    "name: vlad".to_string(), 
                    "secret".to_string()
                ), 
                "secret".to_string()
            ),
            "name: vlad".to_string()
        )
    }

}