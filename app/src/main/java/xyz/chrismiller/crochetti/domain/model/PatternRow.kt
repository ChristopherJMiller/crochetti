package xyz.chrismiller.crochetti.domain.model

import kotlinx.serialization.Serializable

/**
 * A single row of instructions in a crochet pattern.
 */
@Serializable
data class PatternRow(
    val id: Long = 0,
    val description: String = "",
    val instructions: List<StitchGroup>,
    val sided: Sided? = null,
    val hasMagicRing: Boolean = false,
    val repeatCount: Int = 1 // 1 = single row, >1 = repeat rows (e.g., "7-9)")
) {
    /**
     * Whether this row represents multiple identical rows.
     */
    val isRepeating: Boolean get() = repeatCount > 1

    /**
     * Total number of stitches this row produces.
     */
    val totalStitchCount: Int
        get() = instructions.sumOf { it.totalStitchCount }

    /**
     * Format the row for display.
     * Example: "On RS. MR, (sc 2, inc) x6. (24 sts). Work in back loops only."
     */
    fun toDisplayString(): String = buildString {
        sided?.let { append("On ${it.abbreviation}. ") }

        if (hasMagicRing) {
            append("MR, ")
        }

        val instructionText = instructions.joinToString(", ") { it.toDisplayString() }
        append(instructionText)

        append(". (${totalStitchCount} sts)")

        if (description.isNotBlank()) {
            append(". ")
            append(description)
        }
    }

    /**
     * Get just the instruction part without sided or description.
     */
    fun instructionsOnlyString(): String {
        return instructions.joinToString(", ") { it.toDisplayString() }
    }

    companion object {
        /**
         * Create a simple row from a single group.
         */
        fun simple(
            stitch: Stitch,
            count: Int,
            description: String = "",
            sided: Sided? = null
        ): PatternRow {
            return PatternRow(
                description = description,
                instructions = listOf(StitchGroup.of(stitch, count)),
                sided = sided
            )
        }
    }
}
