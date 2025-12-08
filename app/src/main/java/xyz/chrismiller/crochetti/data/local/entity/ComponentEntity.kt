package xyz.chrismiller.crochetti.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "components",
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
data class ComponentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patternId: Long,
    val name: String,
    val sortOrder: Int = 0
)
