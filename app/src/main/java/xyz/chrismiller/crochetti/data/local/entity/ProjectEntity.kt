package xyz.chrismiller.crochetti.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "projects",
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
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patternId: Long,
    val name: String,
    val status: String = "active", // "active" | "completed" | "paused"
    val currentComponentId: Long? = null,
    val currentRowIndex: Int = 0,
    val currentStitchCount: Int = 0,
    val completedRowIdsJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
