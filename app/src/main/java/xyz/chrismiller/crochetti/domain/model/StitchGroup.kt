package xyz.chrismiller.crochetti.domain.model

import kotlinx.serialization.Serializable

/**
 * A group of stitches that may repeat.
 * Example: "(sc 2, inc) x6" = StitchGroup with 3 stitches, repeated 6 times
 */
@Serializable
data class StitchGroup(
    val stitches: List<Stitch>,
    val repetitions: Int = 1
) {
    /**
     * Total stitch count produced by this group (accounting for repetitions).
     */
    val totalStitchCount: Int
        get() = stitches.sumOf { it.stitchCount } * repetitions

    /**
     * Format this group for display.
     * Examples:
     * - Single stitch, no repeat: "sc"
     * - Single stitch with count: "sc 6"
     * - Group with repeat: "(sc 2, inc) x6"
     */
    fun toDisplayString(): String {
        if (stitches.isEmpty()) return ""

        // Group consecutive identical stitches
        val grouped = groupConsecutiveStitches()

        val inner = grouped.joinToString(", ") { (stitch, count) ->
            if (count > 1) "${stitch.abbreviation} $count" else stitch.abbreviation
        }

        val needsParens = grouped.size > 1 || (grouped.size == 1 && repetitions > 1 && grouped[0].second > 1)

        return buildString {
            if (needsParens && repetitions > 1) append("(")
            append(inner)
            if (needsParens && repetitions > 1) append(")")
            if (repetitions > 1) append(" x$repetitions")
        }
    }

    /**
     * Groups consecutive identical stitches together.
     * [sc, sc, sc, inc] -> [(sc, 3), (inc, 1)]
     */
    private fun groupConsecutiveStitches(): List<Pair<Stitch, Int>> {
        if (stitches.isEmpty()) return emptyList()

        val result = mutableListOf<Pair<Stitch, Int>>()
        var current = stitches[0]
        var count = 1

        for (i in 1 until stitches.size) {
            if (stitches[i] == current) {
                count++
            } else {
                result.add(current to count)
                current = stitches[i]
                count = 1
            }
        }
        result.add(current to count)

        return result
    }

    companion object {
        /**
         * Create a simple group of identical stitches.
         */
        fun of(stitch: Stitch, count: Int = 1, repetitions: Int = 1): StitchGroup {
            return StitchGroup(
                stitches = List(count) { stitch },
                repetitions = repetitions
            )
        }
    }
}
