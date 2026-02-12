use hex_simd::AsciiCase::Upper;

pub fn to_hex(data:&[u8]) ->String{
    if !data.is_empty() {
        hex_simd::encode_to_string(data, Upper)
    }else {
       "".to_string()
    }
}


pub fn decode_hex(encoding:String)->Vec<u8> {
    if !encoding.is_empty() {
        hex_simd::decode_to_vec(encoding.as_bytes()).unwrap()
    } else {
        vec![]
    }
}

#[cfg(test)]
mod common_test {
    use crate::cipher::common::decode_hex;

    #[test]
    fn test_to_hex(){
        let data = b"abc";
        let hex = crate::cipher::common::to_hex(data);
        println!("hex {}",hex);
        let vec = decode_hex(hex);
        println!("vec {:?}",vec);
    }


}