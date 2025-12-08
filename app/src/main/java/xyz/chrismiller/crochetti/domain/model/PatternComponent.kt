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
     * Total number of rows in this component.
     */
    val totalRows: Int get() = rows.size

    /**
     * Total number of stitches across all rows.
     */
    val totalStitches: Int get() = rows.sumOf { it.totalStitchCount }
}
