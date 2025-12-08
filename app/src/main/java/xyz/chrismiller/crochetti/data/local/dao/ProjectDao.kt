package xyz.chrismiller.crochetti.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import xyz.chrismiller.crochetti.data.local.entity.PatternWithProjects
import xyz.chrismiller.crochetti.data.local.entity.ProjectEntity
import xyz.chrismiller.crochetti.data.local.entity.ProjectWithPattern

@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects WHERE id = :projectId")
    fun getProject(projectId: Long): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectSync(projectId: Long): ProjectEntity?

    @Query("SELECT * FROM projects WHERE patternId = :patternId ORDER BY lastUpdated DESC")
    fun getProjectsForPattern(patternId: Long): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE patternId = :patternId ORDER BY lastUpdated DESC")
    suspend fun getProjectsForPatternSync(patternId: Long): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE status = 'active' ORDER BY lastUpdated DESC")
    fun getActiveProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT COUNT(*) FROM projects WHERE patternId = :patternId AND status = 'active'")
    suspend fun countActiveProjectsForPattern(patternId: Long): Int

    @Query("SELECT COUNT(*) FROM projects WHERE patternId = :patternId AND status = 'completed'")
    suspend fun countCompletedProjectsForPattern(patternId: Long): Int

    @Query("SELECT COUNT(*) FROM projects WHERE patternId = :patternId")
    suspend fun countProjectsForPattern(patternId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("""
        UPDATE projects
        SET currentStitchCount = currentStitchCount + 1,
            lastUpdated = :timestamp
        WHERE id = :projectId
    """)
    suspend fun incrementStitchCounter(projectId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE projects
        SET currentStitchCount = MAX(0, currentStitchCount - 1),
            lastUpdated = :timestamp
        WHERE id = :projectId
    """)
    suspend fun decrementStitchCounter(projectId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE projects
        SET currentRowIndex = :newRowIndex,
            currentStitchCount = 0,
            lastUpdated = :timestamp
        WHERE id = :projectId
    """)
    suspend fun advanceToRow(projectId: Long, newRowIndex: Int, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE projects
        SET currentComponentId = :componentId,
            currentRowIndex = 0,
            currentStitchCount = 0,
            lastUpdated = :timestamp
        WHERE id = :projectId
    """)
    suspend fun selectComponent(projectId: Long, componentId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE projects
        SET status = 'completed',
            completedAt = :timestamp,
            lastUpdated = :timestamp
        WHERE id = :projectId
    """)
    suspend fun markCompleted(projectId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProject(projectId: Long)

    @Transaction
    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectWithPattern(projectId: Long): ProjectWithPattern?

    @Transaction
    @Query("SELECT * FROM patterns ORDER BY updatedAt DESC")
    fun getAllPatternsWithProjects(): Flow<List<PatternWithProjects>>
}
