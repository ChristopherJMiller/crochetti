package xyz.chrismiller.crochetti.domain.model

/**
 * Tracks progress through a pattern.
 */
data class Progress(
    val patternId: Long,
    val currentComponentId: Long? = null,
    val currentRowIndex: Int = 0,
    val currentStitchCount: Int = 0,
    val completedRowIds: Set<Long> = emptySet(),
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Check if a specific row is completed.
     */
    fun isRowCompleted(rowId: Long): Boolean = rowId in completedRowIds

    /**
     * Create a copy with the stitch counter incremented.
     */
    fun incrementStitch(): Progress = copy(
        currentStitchCount = currentStitchCount + 1,
        lastUpdated = System.currentTimeMillis()
    )

    /**
     * Create a copy with the stitch counter decremented (min 0).
     */
    fun decrementStitch(): Progress = copy(
        currentStitchCount = maxOf(0, currentStitchCount - 1),
        lastUpdated = System.currentTimeMillis()
    )

    /**
     * Create a copy with the stitch counter reset.
     */
    fun resetStitchCounter(): Progress = copy(
        currentStitchCount = 0,
        lastUpdated = System.currentTimeMillis()
    )

    /**
     * Create a copy marking the current row as complete and advancing to the next.
     */
    fun completeRowAndAdvance(currentRowId: Long, nextRowIndex: Int): Progress = copy(
        currentRowIndex = nextRowIndex,
        currentStitchCount = 0,
        completedRowIds = completedRowIds + currentRowId,
        lastUpdated = System.currentTimeMillis()
    )

    /**
     * Create a copy with a new component selected.
     */
    fun selectComponent(componentId: Long): Progress = copy(
        currentComponentId = componentId,
        currentRowIndex = 0,
        currentStitchCount = 0,
        lastUpdated = System.currentTimeMillis()
    )

    companion object {
        fun forPattern(patternId: Long, firstComponentId: Long?): Progress {
            return Progress(
                patternId = patternId,
                currentComponentId = firstComponentId,
                currentRowIndex = 0,
                currentStitchCount = 0
            )
        }
    }
}
