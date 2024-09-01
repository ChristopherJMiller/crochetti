use std::collections::HashMap;

use material::Material;
use row::PatternRow;
use serde::{Deserialize, Serialize};
use stitch::CustomStitch;

pub mod material;
pub mod row;
pub mod stitch;

#[derive(Serialize, Deserialize)]
pub struct PatternComponent {
    pub name: String,
    pub instructions: Vec<PatternRow>,
}

#[derive(Serialize, Deserialize)]
pub struct Pattern {
    pub name: String,
    pub description: String,
    pub custom_stitches: HashMap<String, CustomStitch>,
    pub materials: Vec<Material>,
    pub components: Vec<PatternComponent>,
}
