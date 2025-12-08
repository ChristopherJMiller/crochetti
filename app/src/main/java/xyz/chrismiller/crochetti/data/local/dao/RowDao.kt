package xyz.chrismiller.crochetti.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import xyz.chrismiller.crochetti.data.local.entity.RowEntity

@Dao
interface RowDao {

    @Query("SELECT * FROM rows WHERE componentId = :componentId ORDER BY sortOrder")
    fun getRowsForComponent(componentId: Long): Flow<List<RowEntity>>

    @Query("SELECT * FROM rows WHERE componentId = :componentId ORDER BY sortOrder")
    suspend fun getRowsForComponentSync(componentId: Long): List<RowEntity>

    @Query("SELECT * FROM rows WHERE id = :id")
    suspend fun getRowById(id: Long): RowEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRow(row: RowEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRows(rows: List<RowEntity>): List<Long>

    @Update
    suspend fun updateRow(row: RowEntity)

    @Delete
    suspend fun deleteRow(row: RowEntity)

    @Query("DELETE FROM rows WHERE componentId = :componentId")
    suspend fun deleteRowsForComponent(componentId: Long)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM rows WHERE componentId = :componentId")
    suspend fun getNextSortOrder(componentId: Long): Int

    @Query("SELECT COUNT(*) FROM rows WHERE componentId = :componentId")
    suspend fun getRowCountForComponent(componentId: Long): Int
}
