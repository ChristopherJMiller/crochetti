use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
pub struct Material {
    pub name: String,
    pub unit: Option<String>,
    pub number: usize,
}
