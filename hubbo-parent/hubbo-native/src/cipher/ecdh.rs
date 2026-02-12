use anyhow::Result;
use aws_lc_rs::agreement::{self, Algorithm, EphemeralPrivateKey, UnparsedPublicKey};
use aws_lc_rs::rand::SystemRandom;
use std::sync::OnceLock;

#[derive(Debug)]
#[allow(unused)]
enum EcdhError {
    KeyExchangeFailed,
    SeedGenerationFailed,
}

static RNG: OnceLock<SystemRandom> = OnceLock::new();
const ALG: &Algorithm = &agreement::X25519;

/**
## 暂时不考虑多种算法 直接使用X25519即可
*/
#[allow(unused)]
pub fn generate_ecdh_private_key() -> Result<EphemeralPrivateKey, Box<dyn std::error::Error>> {
    let rng = RNG.get_or_init(SystemRandom::new);
    Ok(EphemeralPrivateKey::generate(ALG, rng)?)
}

/**
## 获取共享密钥
#### @private_key: 私钥
#### @public_key: 公钥
#### @return: 共享密钥
#### @link:https://docs.rs/aws-lc-rs/1.15.4/aws_lc_rs/agreement/index.html
*/
#[allow(unused)]
pub fn get_secret(
    private_key: EphemeralPrivateKey,
    public_key: &[u8],
) -> Result<Vec<u8>, Box<dyn std::error::Error>> {
    let secret = agreement::agree_ephemeral(
        private_key,
        &UnparsedPublicKey::new(ALG, public_key),
        EcdhError::KeyExchangeFailed,
        |material| {
            // material 是 SharedKeyMaterial，具有保护性，不能直接脱离闭包
            Ok(material.to_vec())
        },
    )
    .map_err(|_| "failed")?;
    println!("生成的密钥长度{} {:?}", secret.len(), secret);
    Ok(secret)
}

#[cfg(test)]
mod ecdh_test {
    use crate::cipher::ecdh::{generate_ecdh_private_key, get_secret};

    #[test]
    fn test_get_ecdh_secret() -> anyhow::Result<(), Box<dyn std::error::Error>> {
        let k1 = generate_ecdh_private_key()?;
        let k2 = generate_ecdh_private_key()?;
        let public_key = k2.compute_public_key()?;
        get_secret(k1, public_key.as_ref())?;
        Ok(())
    }
}
