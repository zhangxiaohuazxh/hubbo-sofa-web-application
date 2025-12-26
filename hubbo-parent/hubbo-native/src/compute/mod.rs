#[unsafe(no_mangle)]
pub extern "C" fn pow(num: i32, exponent: u32) -> i32 {
    num.pow(exponent)
}
