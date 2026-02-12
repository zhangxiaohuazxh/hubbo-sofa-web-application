#[unsafe(no_mangle)]
pub extern "C" fn pow(num: i32, exponent: u32) -> i32 {
    num.pow(exponent)
}

#[cfg(target_family = "wasm")]
mod wasm {
    use wasm_bindgen::prelude::wasm_bindgen;

    #[wasm_bindgen]
    pub fn plus(num1: i32, num2: i32) -> i32 {
        num1 + num2
    }
}
