use anyhow::Result;
use aws_lc_rs::aead::{AES_256_GCM, Aad, Nonce, RandomizedNonceKey};

#[allow(unused)]
fn encrypt_with_aes_256_gcm(
    secret: &[u8],
    plaintext: &[u8],
) -> Result<Vec<u8>, Box<dyn std::error::Error>> {
    let key = RandomizedNonceKey::new(&AES_256_GCM, secret)?;
    let mut in_out = Vec::from(plaintext);
    // 1. 加密并追加Tag
    let nonce = key.seal_in_place_append_tag(Aad::empty(), &mut in_out)?;
    // 2. 关键：将Nonce(12字节)拼接到结果的最前面
    let mut result = Vec::with_capacity(12 + in_out.len());
    result.extend_from_slice(nonce.as_ref());
    result.extend_from_slice(in_out.as_ref());
    Ok(result)
}

#[allow(unused)]
fn decrypt_with_aes_256_gcm(
    secret: &[u8],
    encrypt_content: &[u8],
) -> Result<Vec<u8>, Box<dyn std::error::Error>> {
    let key = RandomizedNonceKey::new(&AES_256_GCM, secret)?;
    // 1. 基础长度校验(12字节Nonce + 16字节Tag)
    if encrypt_content.len() < 12 + AES_256_GCM.tag_len() {
        return Err("数据长度不足".into());
    }
    // 2. 提取前12字节作为 Nonce
    let (nonce_part, ciphertext_part) = encrypt_content.split_at(12);
    let nonce_bytes: [u8; 12] = nonce_part.try_into()?;
    let nonce = Nonce::assume_unique_for_key(nonce_bytes);
    // 3. 准备缓冲区进行原地解密
    let mut ciphertext_with_tag = ciphertext_part.to_vec();
    // 3. 执行解密
    // open_in_place 会验证末尾的 Tag 并原地解密
    let plaintext_slice = key
        .open_in_place(nonce, Aad::empty(), &mut ciphertext_with_tag)
        .map_err(|_| "解密失败：数据被篡改或密钥错误")?;
    Ok(plaintext_slice.to_vec())
}

#[allow(unused)]
fn encrypt_with_aes_256_gcm_hex(
    secret: &[u8],
    plaintext: &[u8],
) -> Result<String, Box<dyn std::error::Error>> {
    let vec = encrypt_with_aes_256_gcm(secret, plaintext)?;
    Ok(super::common::to_hex(&vec))
}

#[allow(unused)]
fn decrypt_with_aes_256_gcm_from_hex(
    secret: &[u8],
    encrypt_content: String,
) -> Result<Vec<u8>, Box<dyn std::error::Error>> {
    Ok(decrypt_with_aes_256_gcm(
        secret,
        &super::common::decode_hex(encrypt_content),
    )?)
}

#[cfg(test)]
mod aes_module_test {
    use crate::cipher::aes::{
        decrypt_with_aes_256_gcm, decrypt_with_aes_256_gcm_from_hex, encrypt_with_aes_256_gcm,
        encrypt_with_aes_256_gcm_hex,
    };
    use crate::cipher::ecdh::{generate_ecdh_private_key, get_secret};
    #[test]
    fn test_simple_aes_encrypt() -> anyhow::Result<(), Box<dyn std::error::Error>> {
        let secret = get_secret(
            generate_ecdh_private_key()?,
            generate_ecdh_private_key()?.compute_public_key()?.as_ref(),
        )?;
        let res = encrypt_with_aes_256_gcm(&secret, b"abc")?;
        println!("加密后的密文长度 {} {:?}", res.len(), res);
        println!(
            "解密后的文本 {:?}",
            decrypt_with_aes_256_gcm(&secret, &res)?
        );
        let encoding_content = encrypt_with_aes_256_gcm_hex(&secret, b"abc")?;
        println!("加密后的文本 {}", encoding_content);
        let vec = decrypt_with_aes_256_gcm_from_hex(&secret, encoding_content)?;
        println!("vec {:?}", vec);
        Ok(())
    }
}
