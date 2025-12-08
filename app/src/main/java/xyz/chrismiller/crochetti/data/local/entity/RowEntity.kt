package xyz.chrismiller.crochetti.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rows",
    foreignKeys = [
        ForeignKey(
            entity = ComponentEntity::class,
            parentColumns = ["id"],
            childColumns = ["componentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("componentId")]
)
data class RowEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val componentId: Long,
    val description: String = "",
    val sided: String? = null, // "RS" or "WS"
    val hasMagicRing: Boolean = false,
    val instructionsJson: String, // JSON serialized List<StitchGroup>
    val stitchCount: Int, // Pre-calculated for display
    val sortOrder: Int = 0,
    val repeatCount: Int = 1 // 1 = single row, >1 = repeat rows (e.g., "7-9)")
)
