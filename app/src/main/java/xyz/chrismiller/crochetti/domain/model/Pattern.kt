package xyz.chrismiller.crochetti.domain.model

/**
 * A complete crochet pattern with metadata and components.
 */
data class Pattern(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val photoUri: String? = null,
    val components: List<PatternComponent> = emptyList(),
    val customStitches: List<CustomStitchDefinition> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Total number of rows across all components.
     */
    val totalRows: Int get() = components.sumOf { it.totalRows }

    /**
     * Get a flat list of all rows with their component context.
     */
    fun getAllRowsFlat(): List<Pair<PatternComponent, PatternRow>> {
        return components.flatMap { component ->
            component.rows.map { row -> component to row }
        }
    }

    /**
     * Get custom stitches as a map for parser use.
     */
    fun getCustomStitchMap(): Map<String, Stitch.Custom> {
        return customStitches.associate {
            it.normalizedAbbreviation to it.toStitch()
        }
    }
}
