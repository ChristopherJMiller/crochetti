package xyz.chrismiller.crochetti.domain.model

/**
 * A component of a pattern (e.g., "Body", "Arms", "Head").
 * Each component has its own set of rows.
 */
data class PatternComponent(
    val id: Long = 0,
    val name: String,
    val rows: List<PatternRow> = emptyList(),
    val sortOrder: Int = 0
) {
    /**
     * Total number of stored rows in this component (not counting repeats).
     */
    val totalRows: Int get() = rows.size

    /**
     * Total number of virtual rows (accounting for repeats).
     * A row with repeatCount=3 contributes 3 to this total.
     */
    val totalVirtualRows: Int get() = rows.sumOf { it.repeatCount }

    /**
     * Total number of stitches across all rows.
     */
    val totalStitches: Int get() = rows.sumOf { it.totalStitchCount }

    /**
     * Expand rows into individual ExpandedRows for progress tracking.
     * Repeat rows are expanded into multiple ExpandedRows sharing the same source.
     *
     * Example: [Row(repeatCount=1), Row(repeatCount=3), Row(repeatCount=1)]
     * Produces: [ExpandedRow#1, ExpandedRow#2, ExpandedRow#3, ExpandedRow#4, ExpandedRow#5]
     *           where #2, #3, #4 all reference the same source row.
     */
    fun expandRows(): List<ExpandedRow> {
        val result = mutableListOf<ExpandedRow>()
        var displayNumber = 1

        for (row in rows) {
            if (row.repeatCount == 1) {
                result.add(
                    ExpandedRow(
                        sourceRow = row,
                        displayNumber = displayNumber,
                        displayLabel = "$displayNumber)",
                        repetitionIndex = 0,
                        isPartOfRepeat = false,
                        repeatRangeLabel = null
                    )
                )
                displayNumber++
            } else {
                val rangeEnd = displayNumber + row.repeatCount - 1
                val rangeLabel = "$displayNumber-$rangeEnd"

                repeat(row.repeatCount) { repIndex ->
                    result.add(
                        ExpandedRow(
                            sourceRow = row,
                            displayNumber = displayNumber + repIndex,
                            displayLabel = "$rangeLabel)",
                            repetitionIndex = repIndex,
                            isPartOfRepeat = true,
                            repeatRangeLabel = rangeLabel
                        )
                    )
                }
                displayNumber = rangeEnd + 1
            }
        }

        return result
    }
}
