package xyz.chrismiller.crochetti.domain.model

/**
 * Category of crochet stitch for organization in the glossary.
 */
enum class StitchCategory(val displayName: String) {
    BASIC("Basic Stitches"),
    TEXTURED("Textured Stitches"),
    POST("Post Stitches"),
    EXTENDED("Extended Stitches"),
    SHAPING("Shaping"),
    OTHER("Other")
}

/**
 * Difficulty level for learning a stitch.
 */
enum class StitchDifficulty(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

/**
 * Complete information about a stitch for the glossary.
 *
 * @property stitch The stitch type this info describes
 * @property category Category for grouping in the glossary
 * @property description What the stitch looks like and its purpose
 * @property instructions Step-by-step how to perform the stitch
 * @property difficulty Skill level required
 * @property aliases Alternative abbreviations that parse to this stitch
 * @property usTerminology US crochet terminology name
 * @property ukEquivalent UK terminology equivalent (if different)
 */
data class StitchInfo(
    val stitch: Stitch,
    val category: StitchCategory,
    val description: String,
    val instructions: String,
    val difficulty: StitchDifficulty,
    val aliases: List<String> = emptyList(),
    val usTerminology: String = stitch.displayName,
    val ukEquivalent: String? = null
)
