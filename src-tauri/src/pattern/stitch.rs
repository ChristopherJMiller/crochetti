use std::fmt::Display;

use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
pub struct StitchGroup {
    pub group: Vec<Stitch>,
    pub n: usize,
}

impl StitchGroup {
    fn build_pattern_tail(acc: String, current_stitch: &Stitch, repeats: usize) -> String {
        let repeat_label = if repeats > 1 {
            format!(" {repeats}")
        } else {
            String::new()
        };

        let comma = if acc.is_empty() {
            String::new()
        } else {
            ", ".to_string()
        };

        format!("{acc}{comma}{}{repeat_label}", current_stitch)
    }
}

impl Display for StitchGroup {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        let (acc, (current_stitch, repeats)) = self
            .group
            .iter()
            .fold(
                (String::new(), (None, 0)),
                |(acc, (current_stitch, repeats)), stitch| {
                    println!("{stitch} -> ({acc}, ({current_stitch:?}, {repeats}))");
                    // Already in queue, need to just increase repeat
                    if current_stitch == Some(stitch) {
                        (acc, (current_stitch, repeats + 1))
                    // No queued stitch, start that now
                    } else if current_stitch == None {
                        (acc, (Some(stitch), 1))
                    // Not in queue, need to empty queue
                    } else if let Some(current_stitch) = current_stitch {
                        (Self::build_pattern_tail(acc, current_stitch, repeats), (Some(stitch), 1))
                    } else {
                        panic!("Error occured during StitchGroup: ({acc}, ({current_stitch:?}, {repeats}))");
                    }
                },
            );

        let inner_pattern = if let Some(current_stitch) = current_stitch {
            Self::build_pattern_tail(acc, current_stitch, repeats)
        } else {
            acc
        };

        let (first, rest) = inner_pattern.split_at(1);
        let inner_pattern = format!("{}{rest}", first.to_uppercase());

        let (left, right) = if self.group.len() > 1 {
            ("(".to_string(), ")".to_string())
        } else {
            (String::new(), String::new())
        };

        let repeat = if self.n > 1 {
            format!(" x{}", self.n)
        } else {
            String::new()
        };

        write!(f, "{left}{inner_pattern}{right}{repeat}")
    }
}

#[derive(Serialize, Deserialize, PartialEq, Debug)]
pub enum Stitch {
    SingleCrochet,
    IncreasingCrochet,
    DecreasingCrochet,
    Slip,
    Chain,
    Custom(String),
}

impl Display for Stitch {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        let abbrev = match self {
            Stitch::SingleCrochet => "sc",
            Stitch::IncreasingCrochet => "sc inc",
            Stitch::DecreasingCrochet => "sc dec",
            Stitch::Slip => "sl st",
            Stitch::Chain => "ch",
            Stitch::Custom(custom) => custom,
        };

        write!(f, "{abbrev}")
    }
}

#[derive(Serialize, Deserialize)]
pub struct CustomStitch {
    pub abbrv: String,
    pub name: String,
    pub description: String,
}

#[cfg(test)]
mod tests {
    use crate::pattern::stitch::{Stitch, StitchGroup};

    #[test]
    fn single_item() {
        assert_eq!(
            StitchGroup {
                group: vec![Stitch::Chain],
                n: 3,
            }
            .to_string(),
            "Ch x3"
        );
    }

    #[test]
    fn double_set() {
        assert_eq!(
            StitchGroup {
                group: vec![Stitch::SingleCrochet, Stitch::IncreasingCrochet],
                n: 2,
            }
            .to_string(),
            "(Sc, sc inc) x2"
        );
    }

    #[test]
    fn clumped_items() {
        assert_eq!(
            StitchGroup {
                group: vec![
                    Stitch::SingleCrochet,
                    Stitch::SingleCrochet,
                    Stitch::IncreasingCrochet
                ],
                n: 2,
            }
            .to_string(),
            "(Sc 2, sc inc) x2"
        );
    }

    #[test]
    fn clumped_items_2() {
        assert_eq!(
            StitchGroup {
                group: vec![
                    Stitch::SingleCrochet,
                    Stitch::SingleCrochet,
                    Stitch::SingleCrochet,
                    Stitch::IncreasingCrochet,
                    Stitch::IncreasingCrochet
                ],
                n: 1,
            }
            .to_string(),
            "(Sc 3, sc inc 2)"
        );
    }
}
