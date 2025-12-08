package xyz.chrismiller.crochetti.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import xyz.chrismiller.crochetti.data.local.entity.PatternEntity
import xyz.chrismiller.crochetti.data.local.entity.PatternWithComponents
import xyz.chrismiller.crochetti.data.local.entity.PatternWithComponentsAndRows
import xyz.chrismiller.crochetti.data.local.entity.PatternWithProgress

@Dao
interface PatternDao {

    @Query("SELECT * FROM patterns ORDER BY updatedAt DESC")
    fun getAllPatterns(): Flow<List<PatternEntity>>

    @Query("SELECT * FROM patterns WHERE id = :id")
    suspend fun getPatternById(id: Long): PatternEntity?

    @Transaction
    @Query("SELECT * FROM patterns WHERE id = :id")
    suspend fun getPatternWithComponents(id: Long): PatternWithComponents?

    @Transaction
    @Query("SELECT * FROM patterns WHERE id = :id")
    suspend fun getPatternWithComponentsAndRows(id: Long): PatternWithComponentsAndRows?

    @Transaction
    @Query("SELECT * FROM patterns WHERE id = :id")
    fun getPatternWithComponentsAndRowsFlow(id: Long): Flow<PatternWithComponentsAndRows?>

    @Transaction
    @Query("SELECT * FROM patterns ORDER BY updatedAt DESC")
    fun getAllPatternsWithProgress(): Flow<List<PatternWithProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: PatternEntity): Long

    @Update
    suspend fun updatePattern(pattern: PatternEntity)

    @Delete
    suspend fun deletePattern(pattern: PatternEntity)

    @Query("DELETE FROM patterns WHERE id = :id")
    suspend fun deletePatternById(id: Long)

    @Query("UPDATE patterns SET updatedAt = :timestamp WHERE id = :id")
    suspend fun updateTimestamp(id: Long, timestamp: Long = System.currentTimeMillis())
}
