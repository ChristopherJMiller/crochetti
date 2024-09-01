use std::fmt::Display;

use serde::{Deserialize, Serialize};

use super::stitch::StitchGroup;

#[derive(Serialize, Deserialize)]
pub enum Sided {
    RightSided,
    WrongSided,
}

impl Display for Sided {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "{}",
            match self {
                Sided::RightSided => "On RS",
                Sided::WrongSided => "On WS",
            }
        )
    }
}

#[derive(Serialize, Deserialize)]
pub struct PatternRow {
    pub description: String,
    pub instructions: Vec<StitchGroup>,
    pub sided: Option<Sided>,
}

impl Display for PatternRow {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        let mut parts = vec![];

        if let Some(sided) = &self.sided {
            parts.push(sided.to_string());
        }

        parts.append(
            &mut self
                .instructions
                .iter()
                .map(|grp| grp.to_string())
                .collect::<Vec<_>>(),
        );

        write!(f, "{}", parts.join(" "))
    }
}
