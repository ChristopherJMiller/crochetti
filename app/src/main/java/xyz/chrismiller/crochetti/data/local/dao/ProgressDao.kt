package xyz.chrismiller.crochetti.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import xyz.chrismiller.crochetti.data.local.entity.ProgressEntity

@Dao
interface ProgressDao {

    @Query("SELECT * FROM progress WHERE patternId = :patternId")
    fun getProgress(patternId: Long): Flow<ProgressEntity?>

    @Query("SELECT * FROM progress WHERE patternId = :patternId")
    suspend fun getProgressSync(patternId: Long): ProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: ProgressEntity)

    @Update
    suspend fun updateProgress(progress: ProgressEntity)

    @Query("""
        UPDATE progress
        SET currentStitchCount = currentStitchCount + 1,
            lastUpdated = :timestamp
        WHERE patternId = :patternId
    """)
    suspend fun incrementStitchCounter(patternId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE progress
        SET currentStitchCount = MAX(0, currentStitchCount - 1),
            lastUpdated = :timestamp
        WHERE patternId = :patternId
    """)
    suspend fun decrementStitchCounter(patternId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE progress
        SET currentRowIndex = :newRowIndex,
            currentStitchCount = 0,
            lastUpdated = :timestamp
        WHERE patternId = :patternId
    """)
    suspend fun advanceToRow(patternId: Long, newRowIndex: Int, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE progress
        SET currentComponentId = :componentId,
            currentRowIndex = 0,
            currentStitchCount = 0,
            lastUpdated = :timestamp
        WHERE patternId = :patternId
    """)
    suspend fun selectComponent(patternId: Long, componentId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM progress WHERE patternId = :patternId")
    suspend fun deleteProgress(patternId: Long)
}
