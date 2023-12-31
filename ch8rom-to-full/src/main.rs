use std::env;
use std::io;
use std::io::prelude::*;
use std::fs::File;

fn main() -> io::Result<()> {
    let args: Vec<String> = env::args().collect();
    let input_file_name = &args[1];
    println!("{}", &args[1]);
    let mut input_file = File::open(input_file_name)?;
    let mut input_file_buffer = Vec::new();
    input_file.read_to_end(&mut input_file_buffer);
    let font_set:[u8;80] = [//fonte esperada por programas na memoria
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
        ];
    let mut memory = [0; 0x1000];
    memory[..80].copy_from_slice(&font_set);
    let len = input_file_buffer.len();
    memory[0x200..0x200+len].copy_from_slice(&input_file_buffer[..]);
    let output_file_name = &args[2];
    let mut output_file = File::create(output_file_name)?;
    output_file.write(&memory)?;
    Ok(())
}
