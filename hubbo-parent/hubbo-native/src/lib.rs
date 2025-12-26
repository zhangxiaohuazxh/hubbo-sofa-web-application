pub mod compute;

pub const VERSION: &'static str = "0.0.1";

pub fn init() -> Result<(), Box<dyn std::error::Error>> {
    // 初始化逻辑
    println!("init execute");
    Ok(())
}
