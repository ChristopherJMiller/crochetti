package xyz.chrismiller.crochetti.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.chrismiller.crochetti.data.local.dao.ProgressDao
import xyz.chrismiller.crochetti.data.local.entity.ProgressEntity
import xyz.chrismiller.crochetti.domain.model.Progress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val progressDao: ProgressDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Get progress for a pattern as a flow.
     */
    fun getProgress(patternId: Long): Flow<Progress?> {
        return progressDao.getProgress(patternId).map { entity ->
            entity?.toDomain()
        }
    }

    /**
     * Get progress synchronously.
     */
    suspend fun getProgressSync(patternId: Long): Progress? {
        return progressDao.getProgressSync(patternId)?.toDomain()
    }

    /**
     * Initialize progress for a pattern.
     */
    suspend fun initializeProgress(patternId: Long, firstComponentId: Long?) {
        progressDao.upsertProgress(
            ProgressEntity(
                patternId = patternId,
                currentComponentId = firstComponentId,
                currentRowIndex = 0,
                currentStitchCount = 0,
                completedRowIdsJson = "[]"
            )
        )
    }

    /**
     * Increment the stitch counter.
     */
    suspend fun incrementStitch(patternId: Long) {
        progressDao.incrementStitchCounter(patternId)
    }

    /**
     * Decrement the stitch counter.
     */
    suspend fun decrementStitch(patternId: Long) {
        progressDao.decrementStitchCounter(patternId)
    }

    /**
     * Advance to a specific row.
     */
    suspend fun advanceToRow(patternId: Long, rowIndex: Int) {
        progressDao.advanceToRow(patternId, rowIndex)
    }

    /**
     * Complete current row and advance to next.
     */
    suspend fun completeRowAndAdvance(patternId: Long, completedRowId: Long, nextRowIndex: Int) {
        val current = progressDao.getProgressSync(patternId)
        if (current != null) {
            val completedIds: MutableSet<Long> = try {
                json.decodeFromString<List<Long>>(current.completedRowIdsJson).toMutableSet()
            } catch (e: Exception) {
                mutableSetOf()
            }
            completedIds.add(completedRowId)

            progressDao.upsertProgress(
                current.copy(
                    currentRowIndex = nextRowIndex,
                    currentStitchCount = 0,
                    completedRowIdsJson = json.encodeToString(completedIds.toList()),
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Select a different component.
     */
    suspend fun selectComponent(patternId: Long, componentId: Long) {
        progressDao.selectComponent(patternId, componentId)
    }

    /**
     * Reset progress for a pattern.
     */
    suspend fun resetProgress(patternId: Long, firstComponentId: Long?) {
        progressDao.upsertProgress(
            ProgressEntity(
                patternId = patternId,
                currentComponentId = firstComponentId,
                currentRowIndex = 0,
                currentStitchCount = 0,
                completedRowIdsJson = "[]",
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    /**
     * Update full progress state.
     */
    suspend fun updateProgress(progress: Progress) {
        progressDao.upsertProgress(progress.toEntity())
    }

    private fun ProgressEntity.toDomain(): Progress {
        val completedIds: Set<Long> = try {
            json.decodeFromString<List<Long>>(completedRowIdsJson).toSet()
        } catch (e: Exception) {
            emptySet()
        }

        return Progress(
            patternId = patternId,
            currentComponentId = currentComponentId,
            currentRowIndex = currentRowIndex,
            currentStitchCount = currentStitchCount,
            completedRowIds = completedIds,
            lastUpdated = lastUpdated
        )
    }

    private fun Progress.toEntity(): ProgressEntity {
        return ProgressEntity(
            patternId = patternId,
            currentComponentId = currentComponentId,
            currentRowIndex = currentRowIndex,
            currentStitchCount = currentStitchCount,
            completedRowIdsJson = json.encodeToString(completedRowIds.toList()),
            lastUpdated = lastUpdated
        )
    }
}
