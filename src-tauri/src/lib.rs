use pattern::stitch::Stitch;
use strum::IntoEnumIterator;

mod pattern;

// Learn more about Tauri commands at https://tauri.app/v1/guides/features/command
#[tauri::command]
fn stitch_list() -> Vec<String> {
    Stitch::iter().map(|x| x.to_string()).collect()
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_fs::init())
        .plugin(tauri_plugin_shell::init())
        .invoke_handler(tauri::generate_handler![stitch_list])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
