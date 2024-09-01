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

impl PatternRow {
    pub fn stitch_count(&self) -> usize {
        self.instructions
            .iter()
            .map(|grp| grp.stitch_count())
            .fold(0, |acc, x| acc + x)
    }
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

        parts.push(format!("({} sts)", self.stitch_count()));

        if !self.description.is_empty() {
            parts.push(self.description.clone());
        }

        write!(f, "{}", parts.join(". "))
    }
}

#[cfg(test)]
mod tests {
    use crate::pattern::{
        row::{PatternRow, Sided},
        stitch::{Stitch, StitchGroup},
    };

    #[test]
    fn not_sided() {
        assert_eq!(
            PatternRow {
                description: "Test Instruction".to_string(),
                instructions: vec![StitchGroup {
                    group: vec![
                        Stitch::SingleCrochet,
                        Stitch::SingleCrochet,
                        Stitch::IncreasingCrochet
                    ],
                    n: 2
                }],
                sided: None,
            }
            .to_string(),
            "(Sc 2, sc inc) x2. (8 sts). Test Instruction"
        );
    }

    #[test]
    fn sided() {
        assert_eq!(
            PatternRow {
                description: "Test Instruction".to_string(),
                instructions: vec![StitchGroup {
                    group: vec![
                        Stitch::SingleCrochet,
                        Stitch::SingleCrochet,
                        Stitch::IncreasingCrochet
                    ],
                    n: 2
                }],
                sided: Some(Sided::RightSided),
            }
            .to_string(),
            "On RS. (Sc 2, sc inc) x2. (8 sts). Test Instruction"
        );
    }

    #[test]
    fn multiple_groups() {
        assert_eq!(
            PatternRow {
                description: "Test Instruction".to_string(),
                instructions: vec![
                    StitchGroup {
                        group: vec![
                            Stitch::SingleCrochet,
                            Stitch::SingleCrochet,
                            Stitch::IncreasingCrochet
                        ],
                        n: 2
                    },
                    StitchGroup {
                        group: vec![Stitch::SingleCrochet, Stitch::SingleCrochet],
                        n: 1
                    }
                ],
                sided: None,
            }
            .to_string(),
            "(Sc 2, sc inc) x2. Sc 2. (10 sts). Test Instruction"
        );
    }
}
