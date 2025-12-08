package xyz.chrismiller.crochetti.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import xyz.chrismiller.crochetti.data.local.entity.CustomStitchEntity

/**
 * DAO for custom stitch definitions.
 */
@Dao
interface CustomStitchDao {

    /**
     * Get all custom stitches for a pattern as a Flow.
     */
    @Query("SELECT * FROM custom_stitches WHERE patternId = :patternId ORDER BY sortOrder")
    fun getCustomStitchesForPattern(patternId: Long): Flow<List<CustomStitchEntity>>

    /**
     * Get all custom stitches for a pattern synchronously.
     */
    @Query("SELECT * FROM custom_stitches WHERE patternId = :patternId ORDER BY sortOrder")
    suspend fun getCustomStitchesForPatternSync(patternId: Long): List<CustomStitchEntity>

    /**
     * Get a custom stitch by its abbreviation within a pattern.
     */
    @Query("SELECT * FROM custom_stitches WHERE patternId = :patternId AND abbreviation = :abbreviation LIMIT 1")
    suspend fun getByAbbreviation(patternId: Long, abbreviation: String): CustomStitchEntity?

    /**
     * Get a custom stitch by ID.
     */
    @Query("SELECT * FROM custom_stitches WHERE id = :id")
    suspend fun getById(id: Long): CustomStitchEntity?

    /**
     * Insert a new custom stitch. Replaces on conflict.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stitch: CustomStitchEntity): Long

    /**
     * Insert multiple custom stitches. Replaces on conflict.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stitches: List<CustomStitchEntity>)

    /**
     * Update an existing custom stitch.
     */
    @Update
    suspend fun update(stitch: CustomStitchEntity)

    /**
     * Delete a custom stitch.
     */
    @Delete
    suspend fun delete(stitch: CustomStitchEntity)

    /**
     * Delete all custom stitches for a pattern.
     */
    @Query("DELETE FROM custom_stitches WHERE patternId = :patternId")
    suspend fun deleteAllForPattern(patternId: Long)

    /**
     * Count custom stitches for a pattern.
     */
    @Query("SELECT COUNT(*) FROM custom_stitches WHERE patternId = :patternId")
    suspend fun countForPattern(patternId: Long): Int
}
