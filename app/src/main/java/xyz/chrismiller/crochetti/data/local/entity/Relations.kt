package xyz.chrismiller.crochetti.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Pattern with all its components (without rows).
 */
data class PatternWithComponents(
    @Embedded val pattern: PatternEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "patternId"
    )
    val components: List<ComponentEntity>
)

/**
 * Component with all its rows.
 */
data class ComponentWithRows(
    @Embedded val component: ComponentEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "componentId"
    )
    val rows: List<RowEntity>
)

/**
 * Full pattern with components and their rows.
 */
data class PatternWithComponentsAndRows(
    @Embedded val pattern: PatternEntity,
    @Relation(
        entity = ComponentEntity::class,
        parentColumn = "id",
        entityColumn = "patternId"
    )
    val componentsWithRows: List<ComponentWithRows>
)

/**
 * Pattern with its progress.
 */
data class PatternWithProgress(
    @Embedded val pattern: PatternEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "patternId"
    )
    val progress: ProgressEntity?
)
