package xyz.chrismiller.crochetti.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import xyz.chrismiller.crochetti.data.local.entity.ComponentEntity
import xyz.chrismiller.crochetti.data.local.entity.ComponentWithRows

@Dao
interface ComponentDao {

    @Query("SELECT * FROM components WHERE patternId = :patternId ORDER BY sortOrder")
    fun getComponentsForPattern(patternId: Long): Flow<List<ComponentEntity>>

    @Query("SELECT * FROM components WHERE id = :id")
    suspend fun getComponentById(id: Long): ComponentEntity?

    @Transaction
    @Query("SELECT * FROM components WHERE id = :id")
    suspend fun getComponentWithRows(id: Long): ComponentWithRows?

    @Transaction
    @Query("SELECT * FROM components WHERE patternId = :patternId ORDER BY sortOrder")
    fun getComponentsWithRowsForPattern(patternId: Long): Flow<List<ComponentWithRows>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponent(component: ComponentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponents(components: List<ComponentEntity>): List<Long>

    @Update
    suspend fun updateComponent(component: ComponentEntity)

    @Delete
    suspend fun deleteComponent(component: ComponentEntity)

    @Query("DELETE FROM components WHERE patternId = :patternId")
    suspend fun deleteComponentsForPattern(patternId: Long)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM components WHERE patternId = :patternId")
    suspend fun getNextSortOrder(patternId: Long): Int
}
