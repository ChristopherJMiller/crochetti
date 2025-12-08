package xyz.chrismiller.crochetti.domain.model

/**
 * Information about a crochet technique (not a stitch, but a method or concept).
 */
data class TechniqueInfo(
    val abbreviation: String,
    val name: String,
    val description: String,
    val instructions: String,
    val tips: String? = null
)

/**
 * Glossary of crochet techniques and concepts.
 */
object TechniqueGlossary {

    val magicRing = TechniqueInfo(
        abbreviation = "MR",
        name = "Magic Ring",
        description = "Also called magic circle or adjustable loop. A technique for starting projects worked in the round that creates a tightly closed center with no hole. Essential for amigurumi and hats.",
        instructions = """
            1. Make a loop with yarn tail on the left, working yarn on top
            2. Insert hook through loop, grab working yarn
            3. Pull through to create first loop on hook
            4. Chain 1 to secure
            5. Work your stitches into the ring (under both strands)
            6. Pull yarn tail to close the ring tightly
            7. Join with slip stitch to first stitch
        """.trimIndent(),
        tips = "Use a stitch marker to mark the first stitch. Leave a 6\" tail for weaving in securely. Tighter yarns work better than fuzzy ones."
    )

    val rightSide = TechniqueInfo(
        abbreviation = "RS",
        name = "Right Side",
        description = "The front or 'public' side of your work that will face outward when finished. On garments, this is the side that shows.",
        instructions = """
            How to identify the Right Side:
            • Row 1 (after foundation chain) is typically the RS
            • Odd-numbered rows are usually RS
            • Stitches have clear 'V' shapes on top
            • Fabric has a flatter, tighter texture
            • Yarn tail is at bottom LEFT (for right-handed crocheters)
        """.trimIndent(),
        tips = "Use a stitch marker clipped to the RS if you have trouble remembering. When working in the round, the RS always faces you."
    )

    val wrongSide = TechniqueInfo(
        abbreviation = "WS",
        name = "Wrong Side",
        description = "The back or 'private' side of your work that will face inward when finished. On garments, this is the side against your body.",
        instructions = """
            How to identify the Wrong Side:
            • Even-numbered rows are usually WS
            • Stitches show horizontal bars below the 'V'
            • Fabric has a bumpier, more textured feel
            • Yarn tail is at bottom RIGHT (for right-handed crocheters)
            • You're working into the 'back' of stitches
        """.trimIndent(),
        tips = "The WS often shows the yarn carries in colorwork, so make sure it faces inward. Some patterns intentionally use the WS as the display side for texture."
    )

    /**
     * Get technique info for a sided value.
     */
    fun getForSided(sided: Sided): TechniqueInfo {
        return when (sided) {
            Sided.RightSide -> rightSide
            Sided.WrongSide -> wrongSide
        }
    }
}
