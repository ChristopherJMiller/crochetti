package xyz.chrismiller.crochetti.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "progress",
    foreignKeys = [
        ForeignKey(
            entity = PatternEntity::class,
            parentColumns = ["id"],
            childColumns = ["patternId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patternId")]
)
data class ProgressEntity(
    @PrimaryKey
    val patternId: Long,
    val currentComponentId: Long? = null,
    val currentRowIndex: Int = 0,
    val currentStitchCount: Int = 0,
    val completedRowIdsJson: String = "[]", // JSON array of Long
    val lastUpdated: Long = System.currentTimeMillis()
)
