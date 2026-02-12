use super::ecdh;

#[repr(C)]
pub struct ArrayPointer {
    ptr1: *const u8,
    len1: usize,
    ptr2: *mut u8,
    len2: usize,
}

/**
## 生成共享密钥
### @param ptr java传递的指针
### @param len java传递的指针长度
## @return 包装了两个数组指针的结构体
*/
#[unsafe(no_mangle)]
pub extern "C" fn generate_shared_secret(ptr: *mut u8, len: usize) -> ArrayPointer {
    let public_key = unsafe { std::slice::from_raw_parts(ptr, len) };
    let private_key = ecdh::generate_ecdh_private_key().expect("生成密钥对失败");
    let server_public_key = private_key.compute_public_key().unwrap().as_ref().to_vec();
    let secret = ecdh::get_secret(private_key, public_key).expect("生成共享密钥失败");
    let arr = ArrayPointer {
        ptr1: server_public_key.as_ptr() as *mut u8,
        len1: server_public_key.len(),
        ptr2: secret.as_ptr() as *mut u8,
        len2: secret.len(),
    };
    std::mem::forget(server_public_key);
    std::mem::forget(secret);
    arr
}

// 必须提供释放函数，供 Java Arena 关闭时回调
#[unsafe(no_mangle)]
pub unsafe extern "C" fn free_variable_vec(ptr: *mut u8, len: usize) {
    if !ptr.is_null() {
        let _ = unsafe { Vec::from_raw_parts(ptr, len, len) };
    }
}
