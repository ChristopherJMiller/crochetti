package xyz.chrismiller.crochetti.domain.model

/**
 * Represents a single row in the expanded (runtime) view of a pattern.
 * For repeat rows, multiple ExpandedRows share the same underlying PatternRow.
 *
 * Example: A PatternRow with repeatCount=3 at position 7 produces:
 * - ExpandedRow(displayNumber=7, repetitionIndex=0, displayLabel="7-9)")
 * - ExpandedRow(displayNumber=8, repetitionIndex=1, displayLabel="7-9)")
 * - ExpandedRow(displayNumber=9, repetitionIndex=2, displayLabel="7-9)")
 */
data class ExpandedRow(
    val sourceRow: PatternRow,
    val displayNumber: Int,         // What row number to show (e.g., 7, 8, 9)
    val displayLabel: String,       // "7)" or "7-9)" for display
    val repetitionIndex: Int,       // 0-based index within the repeat (0, 1, 2 for 3 reps)
    val isPartOfRepeat: Boolean,    // true if repeatCount > 1
    val repeatRangeLabel: String?   // "7-9" for repeat rows, null for single
) {
    /**
     * Unique identifier for progress tracking.
     * Combines row ID with repetition index for repeat rows.
     */
    val progressKey: String get() = "${sourceRow.id}_$repetitionIndex"

    /**
     * Format for display during progress: "Row 7" or "Row 7-9 (2 of 3)"
     */
    fun toProgressDisplayString(): String {
        return if (isPartOfRepeat) {
            "Row $repeatRangeLabel (${repetitionIndex + 1} of ${sourceRow.repeatCount})"
        } else {
            "Row $displayNumber"
        }
    }

    /**
     * Get the source row's instructions as a display string.
     */
    fun instructionsDisplayString(): String = sourceRow.toDisplayString()
}
