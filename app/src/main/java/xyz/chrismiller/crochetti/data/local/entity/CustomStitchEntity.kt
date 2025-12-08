package xyz.chrismiller.crochetti.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for storing custom stitch definitions.
 *
 * Custom stitches are pattern-scoped, allowing each pattern to define
 * its own specialized stitches using a DSL.
 */
@Entity(
    tableName = "custom_stitches",
    foreignKeys = [
        ForeignKey(
            entity = PatternEntity::class,
            parentColumns = ["id"],
            childColumns = ["patternId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("patternId"),
        Index(value = ["patternId", "abbreviation"], unique = true)
    ]
)
data class CustomStitchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** The pattern this custom stitch belongs to */
    val patternId: Long,

    /** Short abbreviation for use in pattern DSL (e.g., "pbo") */
    val abbreviation: String,

    /** Human-readable display name (e.g., "Partial Bobble") */
    val displayName: String,

    /** Optional description of the stitch */
    val description: String = "",

    /** JSON-serialized List<StitchInstruction> defining the stitch mechanics */
    val instructionsJson: String,

    /** Number of stitches this custom stitch produces */
    val stitchCount: Int,

    /** Original DSL text for re-editing in the UI */
    val rawDsl: String,

    /** Order in which custom stitches appear in the UI */
    val sortOrder: Int = 0
)
