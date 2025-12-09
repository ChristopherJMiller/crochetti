package xyz.chrismiller.crochetti.domain.model

/**
 * Represents the current position within a row's stitch sequence.
 * Derived from the current stitch count and the row's instructions.
 */
data class StitchPosition(
    val groupIndex: Int,              // Which StitchGroup (0-based)
    val groupRepetition: Int,         // Current repetition of this group (1-based for display)
    val totalGroupRepetitions: Int,   // Total repetitions for this group
    val stitchIndexInGroup: Int,      // Which stitch in the group's pattern (0-based)
    val subStitchProgress: Int,       // Progress within multi-count stitch (1-based)
    val totalSubStitches: Int,        // Total sub-stitches for current stitch (e.g., 2 for inc)
    val currentStitch: Stitch,        // The actual stitch object
    val consecutiveCount: Int,        // How many consecutive same stitches (e.g., 6 for "sc 6")
    val positionInConsecutive: Int,   // Position within consecutive run (1-based)
    val previousStitch: AdjacentStitchInfo?,  // Info about previous stitch for display
    val nextStitch: AdjacentStitchInfo?       // Info about next stitch for display
) {
    /**
     * Whether we're in a repeating group (repetitions > 1)
     */
    val isInRepeatingGroup: Boolean get() = totalGroupRepetitions > 1

    /**
     * Whether the current stitch has multiple sub-stitches (like inc = 2)
     */
    val hasMultipleSubStitches: Boolean get() = totalSubStitches > 1

    /**
     * Whether we're in a run of consecutive identical stitches
     */
    val isInConsecutiveRun: Boolean get() = consecutiveCount > 1
}

/**
 * Simplified stitch info for prev/next display in the position indicator
 */
data class AdjacentStitchInfo(
    val stitch: Stitch,
    val consecutiveCount: Int,  // How many in a row (1 if single)
    val isInRepeatingGroup: Boolean,
    val groupRepetitions: Int
)

/**
 * Derives the current stitch position from the count and instructions.
 * Returns null if count is 0 (not started) or exceeds total stitches.
 */
fun deriveStitchPosition(
    currentCount: Int,
    instructions: List<StitchGroup>
): StitchPosition? {
    if (instructions.isEmpty() || currentCount <= 0) return null

    var remaining = currentCount
    var globalStitchIndex = 0

    for ((groupIndex, group) in instructions.withIndex()) {
        for (rep in 0 until group.repetitions) {
            // Find consecutive runs within this group for display purposes
            val consecutiveRuns = groupConsecutiveStitches(group.stitches)

            var stitchIndexInGroup = 0
            for ((runStitch, runCount) in consecutiveRuns) {
                // Each stitch in the run
                for (posInRun in 1..runCount) {
                    val stitchCount = runStitch.stitchCount

                    // Check if we're within this stitch
                    if (remaining <= stitchCount) {
                        // Found the current position
                        return StitchPosition(
                            groupIndex = groupIndex,
                            groupRepetition = rep + 1,
                            totalGroupRepetitions = group.repetitions,
                            stitchIndexInGroup = stitchIndexInGroup,
                            subStitchProgress = remaining,
                            totalSubStitches = stitchCount,
                            currentStitch = runStitch,
                            consecutiveCount = runCount,
                            positionInConsecutive = posInRun,
                            previousStitch = findPreviousStitch(
                                instructions, groupIndex, rep, stitchIndexInGroup, posInRun, consecutiveRuns
                            ),
                            nextStitch = findNextStitch(
                                instructions, groupIndex, rep, stitchIndexInGroup, posInRun, runCount, consecutiveRuns
                            )
                        )
                    }

                    remaining -= stitchCount
                    stitchIndexInGroup++
                    globalStitchIndex++
                }
            }
        }
    }

    // Count exceeds total stitches
    return null
}

/**
 * Groups consecutive identical stitches together.
 * [sc, sc, sc, inc] -> [(sc, 3), (inc, 1)]
 */
private fun groupConsecutiveStitches(stitches: List<Stitch>): List<Pair<Stitch, Int>> {
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

private fun findPreviousStitch(
    instructions: List<StitchGroup>,
    currentGroupIndex: Int,
    currentRep: Int,
    currentStitchIndex: Int,
    positionInRun: Int,
    currentGroupRuns: List<Pair<Stitch, Int>>
): AdjacentStitchInfo? {
    // If we're past the first in a consecutive run, previous is same stitch type
    if (positionInRun > 1) {
        val currentRun = findRunForStitchIndex(currentGroupRuns, currentStitchIndex)
        if (currentRun != null) {
            return AdjacentStitchInfo(
                stitch = currentRun.first,
                consecutiveCount = currentRun.second,
                isInRepeatingGroup = instructions[currentGroupIndex].repetitions > 1,
                groupRepetitions = instructions[currentGroupIndex].repetitions
            )
        }
    }

    // Find previous run in this group's repetition
    var stitchIdx = 0
    var prevRun: Pair<Stitch, Int>? = null
    for (run in currentGroupRuns) {
        if (stitchIdx + run.second > currentStitchIndex) {
            break
        }
        prevRun = run
        stitchIdx += run.second
    }

    if (prevRun != null) {
        return AdjacentStitchInfo(
            stitch = prevRun.first,
            consecutiveCount = prevRun.second,
            isInRepeatingGroup = instructions[currentGroupIndex].repetitions > 1,
            groupRepetitions = instructions[currentGroupIndex].repetitions
        )
    }

    // Look at previous repetition of this group
    if (currentRep > 0) {
        val lastRun = currentGroupRuns.lastOrNull() ?: return null
        return AdjacentStitchInfo(
            stitch = lastRun.first,
            consecutiveCount = lastRun.second,
            isInRepeatingGroup = instructions[currentGroupIndex].repetitions > 1,
            groupRepetitions = instructions[currentGroupIndex].repetitions
        )
    }

    // Look at previous group
    if (currentGroupIndex > 0) {
        val prevGroup = instructions[currentGroupIndex - 1]
        val prevGroupRuns = groupConsecutiveStitches(prevGroup.stitches)
        val lastRun = prevGroupRuns.lastOrNull() ?: return null
        return AdjacentStitchInfo(
            stitch = lastRun.first,
            consecutiveCount = lastRun.second,
            isInRepeatingGroup = prevGroup.repetitions > 1,
            groupRepetitions = prevGroup.repetitions
        )
    }

    return null
}

private fun findNextStitch(
    instructions: List<StitchGroup>,
    currentGroupIndex: Int,
    currentRep: Int,
    currentStitchIndex: Int,
    positionInRun: Int,
    runCount: Int,
    currentGroupRuns: List<Pair<Stitch, Int>>
): AdjacentStitchInfo? {
    val currentGroup = instructions[currentGroupIndex]

    // If we're not at the last in a consecutive run, next is same stitch type
    if (positionInRun < runCount) {
        val currentRun = findRunForStitchIndex(currentGroupRuns, currentStitchIndex)
        if (currentRun != null) {
            return AdjacentStitchInfo(
                stitch = currentRun.first,
                consecutiveCount = currentRun.second,
                isInRepeatingGroup = currentGroup.repetitions > 1,
                groupRepetitions = currentGroup.repetitions
            )
        }
    }

    // Find next run in this group's repetition
    var stitchIdx = 0
    var foundCurrent = false
    for (run in currentGroupRuns) {
        if (foundCurrent) {
            return AdjacentStitchInfo(
                stitch = run.first,
                consecutiveCount = run.second,
                isInRepeatingGroup = currentGroup.repetitions > 1,
                groupRepetitions = currentGroup.repetitions
            )
        }
        if (stitchIdx <= currentStitchIndex && currentStitchIndex < stitchIdx + run.second) {
            foundCurrent = true
        }
        stitchIdx += run.second
    }

    // Look at next repetition of this group
    if (currentRep < currentGroup.repetitions - 1) {
        val firstRun = currentGroupRuns.firstOrNull() ?: return null
        return AdjacentStitchInfo(
            stitch = firstRun.first,
            consecutiveCount = firstRun.second,
            isInRepeatingGroup = currentGroup.repetitions > 1,
            groupRepetitions = currentGroup.repetitions
        )
    }

    // Look at next group
    if (currentGroupIndex < instructions.size - 1) {
        val nextGroup = instructions[currentGroupIndex + 1]
        val nextGroupRuns = groupConsecutiveStitches(nextGroup.stitches)
        val firstRun = nextGroupRuns.firstOrNull() ?: return null
        return AdjacentStitchInfo(
            stitch = firstRun.first,
            consecutiveCount = firstRun.second,
            isInRepeatingGroup = nextGroup.repetitions > 1,
            groupRepetitions = nextGroup.repetitions
        )
    }

    return null
}

private fun findRunForStitchIndex(
    runs: List<Pair<Stitch, Int>>,
    stitchIndex: Int
): Pair<Stitch, Int>? {
    var idx = 0
    for (run in runs) {
        if (idx <= stitchIndex && stitchIndex < idx + run.second) {
            return run
        }
        idx += run.second
    }
    return null
}
