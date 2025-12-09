package xyz.chrismiller.crochetti.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.chrismiller.crochetti.data.local.dao.ComponentDao
import xyz.chrismiller.crochetti.data.local.dao.PatternDao
import xyz.chrismiller.crochetti.data.local.dao.ProjectDao
import xyz.chrismiller.crochetti.data.local.entity.ProjectEntity
import xyz.chrismiller.crochetti.domain.model.Project
import xyz.chrismiller.crochetti.domain.model.ProjectStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao,
    private val patternDao: PatternDao,
    private val componentDao: ComponentDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getProject(projectId: Long): Flow<Project?> {
        return projectDao.getProject(projectId).map { it?.toDomain() }
    }

    suspend fun getProjectSync(projectId: Long): Project? {
        return projectDao.getProjectSync(projectId)?.toDomain()
    }

    fun getProjectsForPattern(patternId: Long): Flow<List<Project>> {
        return projectDao.getProjectsForPattern(patternId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getProjectsForPatternSync(patternId: Long): List<Project> {
        return projectDao.getProjectsForPatternSync(patternId).map { it.toDomain() }
    }

    suspend fun createProject(patternId: Long, customName: String? = null): Long {
        val pattern = patternDao.getPatternById(patternId)
            ?: throw IllegalArgumentException("Pattern not found")
        val existingCount = projectDao.countProjectsForPattern(patternId)
        val projectNumber = existingCount + 1

        val components = componentDao.getComponentsForPattern(patternId).first()
        val firstComponentId = components.minByOrNull { it.sortOrder }?.id

        val projectName = customName ?: if (projectNumber == 1) pattern.name else "${pattern.name} #$projectNumber"

        return projectDao.upsertProject(
            ProjectEntity(
                patternId = patternId,
                name = projectName,
                status = "active",
                currentComponentId = firstComponentId,
                currentRowIndex = 0,
                currentStitchCount = 0,
                completedRowIdsJson = "[]",
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    suspend fun incrementStitch(projectId: Long) {
        projectDao.incrementStitchCounter(projectId)
    }

    suspend fun decrementStitch(projectId: Long) {
        projectDao.decrementStitchCounter(projectId)
    }

    suspend fun advanceToRow(projectId: Long, rowIndex: Int) {
        projectDao.advanceToRow(projectId, rowIndex)
    }

    suspend fun completeRowAndAdvance(projectId: Long, progressKey: String, nextRowIndex: Int) {
        val current = projectDao.getProjectSync(projectId) ?: return
        val completedKeys: MutableSet<String> = try {
            json.decodeFromString<List<String>>(current.completedRowIdsJson).toMutableSet()
        } catch (e: Exception) {
            // Handle migration from old Long format
            try {
                json.decodeFromString<List<Long>>(current.completedRowIdsJson)
                    .map { "${it}_0" }  // Convert old format to new with _0 suffix
                    .toMutableSet()
            } catch (e2: Exception) {
                mutableSetOf()
            }
        }
        completedKeys.add(progressKey)

        projectDao.upsertProject(
            current.copy(
                currentRowIndex = nextRowIndex,
                currentStitchCount = 0,
                completedRowIdsJson = json.encodeToString(completedKeys.toList()),
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    suspend fun selectComponent(projectId: Long, componentId: Long) {
        projectDao.selectComponent(projectId, componentId)
    }

    suspend fun markProjectCompleted(projectId: Long) {
        projectDao.markCompleted(projectId)
    }

    suspend fun resetProject(projectId: Long) {
        val current = projectDao.getProjectSync(projectId) ?: return
        val components = componentDao.getComponentsForPattern(current.patternId).first()
        val firstComponentId = components.minByOrNull { it.sortOrder }?.id

        projectDao.upsertProject(
            current.copy(
                status = "active",
                currentComponentId = firstComponentId,
                currentRowIndex = 0,
                currentStitchCount = 0,
                completedRowIdsJson = "[]",
                lastUpdated = System.currentTimeMillis(),
                completedAt = null
            )
        )
    }

    suspend fun deleteProject(projectId: Long) {
        projectDao.deleteProject(projectId)
    }

    suspend fun updateProjectName(projectId: Long, newName: String) {
        val current = projectDao.getProjectSync(projectId) ?: return
        projectDao.upsertProject(current.copy(name = newName, lastUpdated = System.currentTimeMillis()))
    }

    suspend fun updateProject(project: Project) {
        projectDao.upsertProject(project.toEntity())
    }

    private fun ProjectEntity.toDomain(): Project {
        val completedKeys: Set<String> = try {
            json.decodeFromString<List<String>>(completedRowIdsJson).toSet()
        } catch (e: Exception) {
            // Handle migration from old Long format
            try {
                json.decodeFromString<List<Long>>(completedRowIdsJson)
                    .map { "${it}_0" }  // Convert old format to new with _0 suffix
                    .toSet()
            } catch (e2: Exception) {
                emptySet()
            }
        }

        return Project(
            id = id,
            patternId = patternId,
            name = name,
            status = ProjectStatus.fromString(status),
            currentComponentId = currentComponentId,
            currentRowIndex = currentRowIndex,
            currentStitchCount = currentStitchCount,
            completedProgressKeys = completedKeys,
            createdAt = createdAt,
            lastUpdated = lastUpdated,
            completedAt = completedAt
        )
    }

    private fun Project.toEntity(): ProjectEntity {
        return ProjectEntity(
            id = id,
            patternId = patternId,
            name = name,
            status = status.name.lowercase(),
            currentComponentId = currentComponentId,
            currentRowIndex = currentRowIndex,
            currentStitchCount = currentStitchCount,
            completedRowIdsJson = json.encodeToString(completedProgressKeys.toList()),
            createdAt = createdAt,
            lastUpdated = lastUpdated,
            completedAt = completedAt
        )
    }
}
