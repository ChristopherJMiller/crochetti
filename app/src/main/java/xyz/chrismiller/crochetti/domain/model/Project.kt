package xyz.chrismiller.crochetti.domain.model

enum class ProjectStatus {
    ACTIVE, COMPLETED, PAUSED;

    companion object {
        fun fromString(value: String?): ProjectStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: ACTIVE
        }
    }
}

data class Project(
    val id: Long = 0,
    val patternId: Long,
    val name: String,
    val status: ProjectStatus = ProjectStatus.ACTIVE,
    val currentComponentId: Long? = null,
    val currentRowIndex: Int = 0,
    val currentStitchCount: Int = 0,
    val completedRowIds: Set<Long> = emptySet(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    val isActive: Boolean get() = status == ProjectStatus.ACTIVE
    val isCompleted: Boolean get() = status == ProjectStatus.COMPLETED

    fun isRowCompleted(rowId: Long): Boolean = rowId in completedRowIds

    fun incrementStitch(): Project = copy(
        currentStitchCount = currentStitchCount + 1,
        lastUpdated = System.currentTimeMillis()
    )

    fun decrementStitch(): Project = copy(
        currentStitchCount = maxOf(0, currentStitchCount - 1),
        lastUpdated = System.currentTimeMillis()
    )

    fun resetStitchCounter(): Project = copy(
        currentStitchCount = 0,
        lastUpdated = System.currentTimeMillis()
    )

    fun completeRowAndAdvance(currentRowId: Long, nextRowIndex: Int): Project = copy(
        currentRowIndex = nextRowIndex,
        currentStitchCount = 0,
        completedRowIds = completedRowIds + currentRowId,
        lastUpdated = System.currentTimeMillis()
    )

    fun selectComponent(componentId: Long): Project = copy(
        currentComponentId = componentId,
        currentRowIndex = 0,
        currentStitchCount = 0,
        lastUpdated = System.currentTimeMillis()
    )

    fun markCompleted(): Project = copy(
        status = ProjectStatus.COMPLETED,
        completedAt = System.currentTimeMillis(),
        lastUpdated = System.currentTimeMillis()
    )

    fun reset(firstComponentId: Long?): Project = copy(
        status = ProjectStatus.ACTIVE,
        currentComponentId = firstComponentId,
        currentRowIndex = 0,
        currentStitchCount = 0,
        completedRowIds = emptySet(),
        completedAt = null,
        lastUpdated = System.currentTimeMillis()
    )

    companion object {
        fun forPattern(patternId: Long, patternName: String, firstComponentId: Long?, projectNumber: Int = 1): Project {
            return Project(
                patternId = patternId,
                name = if (projectNumber == 1) patternName else "$patternName #$projectNumber",
                currentComponentId = firstComponentId
            )
        }
    }
}
