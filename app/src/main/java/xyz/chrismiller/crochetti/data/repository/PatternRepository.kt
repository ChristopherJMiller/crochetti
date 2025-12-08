package xyz.chrismiller.crochetti.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.chrismiller.crochetti.data.local.dao.ComponentDao
import xyz.chrismiller.crochetti.data.local.dao.CustomStitchDao
import xyz.chrismiller.crochetti.data.local.dao.PatternDao
import xyz.chrismiller.crochetti.data.local.dao.ProgressDao
import xyz.chrismiller.crochetti.data.local.dao.RowDao
import xyz.chrismiller.crochetti.data.local.entity.ComponentEntity
import xyz.chrismiller.crochetti.data.local.entity.CustomStitchEntity
import xyz.chrismiller.crochetti.data.local.entity.PatternEntity
import xyz.chrismiller.crochetti.data.local.entity.ProgressEntity
import xyz.chrismiller.crochetti.data.local.entity.RowEntity
import xyz.chrismiller.crochetti.domain.model.CustomStitchDefinition
import xyz.chrismiller.crochetti.domain.model.Pattern
import xyz.chrismiller.crochetti.domain.model.PatternComponent
import xyz.chrismiller.crochetti.domain.model.PatternRow
import xyz.chrismiller.crochetti.domain.model.Progress
import xyz.chrismiller.crochetti.domain.model.Sided
import xyz.chrismiller.crochetti.domain.model.StitchGroup
import xyz.chrismiller.crochetti.domain.model.StitchInstruction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatternRepository @Inject constructor(
    private val patternDao: PatternDao,
    private val componentDao: ComponentDao,
    private val rowDao: RowDao,
    private val progressDao: ProgressDao,
    private val customStitchDao: CustomStitchDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Get all patterns with their progress info.
     */
    fun getAllPatternsWithProgress(): Flow<List<Pair<Pattern, Progress?>>> {
        return patternDao.getAllPatternsWithProgress().map { list ->
            list.map { patternWithProgress ->
                val pattern = patternWithProgress.pattern.toDomain()
                val progress = patternWithProgress.progress?.toDomain()
                pattern to progress
            }
        }
    }

    /**
     * Get a complete pattern with all components, rows, and custom stitches.
     */
    suspend fun getPatternWithDetails(patternId: Long): Pattern? {
        val patternWithData = patternDao.getPatternWithComponentsAndRows(patternId) ?: return null

        val components = patternWithData.componentsWithRows
            .sortedBy { it.component.sortOrder }
            .map { componentWithRows ->
                PatternComponent(
                    id = componentWithRows.component.id,
                    name = componentWithRows.component.name,
                    sortOrder = componentWithRows.component.sortOrder,
                    rows = componentWithRows.rows
                        .sortedBy { it.sortOrder }
                        .map { it.toDomain() }
                )
            }

        // Load custom stitches
        val customStitches = customStitchDao.getCustomStitchesForPatternSync(patternId)
            .map { it.toDomain() }

        return Pattern(
            id = patternWithData.pattern.id,
            name = patternWithData.pattern.name,
            description = patternWithData.pattern.description,
            photoUri = patternWithData.pattern.photoUri,
            components = components,
            customStitches = customStitches,
            createdAt = patternWithData.pattern.createdAt,
            updatedAt = patternWithData.pattern.updatedAt
        )
    }

    /**
     * Get pattern with details as a flow.
     */
    fun getPatternWithDetailsFlow(patternId: Long): Flow<Pattern?> {
        return patternDao.getPatternWithComponentsAndRowsFlow(patternId).map { patternWithData ->
            patternWithData?.let {
                val components = it.componentsWithRows
                    .sortedBy { cwr -> cwr.component.sortOrder }
                    .map { componentWithRows ->
                        PatternComponent(
                            id = componentWithRows.component.id,
                            name = componentWithRows.component.name,
                            sortOrder = componentWithRows.component.sortOrder,
                            rows = componentWithRows.rows
                                .sortedBy { row -> row.sortOrder }
                                .map { row -> row.toDomain() }
                        )
                    }

                // Load custom stitches
                val customStitches = customStitchDao.getCustomStitchesForPatternSync(patternId)
                    .map { entity -> entity.toDomain() }

                Pattern(
                    id = it.pattern.id,
                    name = it.pattern.name,
                    description = it.pattern.description,
                    photoUri = it.pattern.photoUri,
                    components = components,
                    customStitches = customStitches,
                    createdAt = it.pattern.createdAt,
                    updatedAt = it.pattern.updatedAt
                )
            }
        }
    }

    /**
     * Save a complete pattern with components, rows, and custom stitches.
     */
    suspend fun savePattern(pattern: Pattern): Long {
        val patternId = patternDao.insertPattern(
            PatternEntity(
                id = if (pattern.id == 0L) 0 else pattern.id,
                name = pattern.name,
                description = pattern.description,
                photoUri = pattern.photoUri,
                createdAt = pattern.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        )

        // Delete existing components if updating
        if (pattern.id != 0L) {
            componentDao.deleteComponentsForPattern(patternId)
            customStitchDao.deleteAllForPattern(patternId)
        }

        // Insert custom stitches
        pattern.customStitches.forEachIndexed { index, customStitch ->
            customStitchDao.insert(
                CustomStitchEntity(
                    patternId = patternId,
                    abbreviation = customStitch.normalizedAbbreviation,
                    displayName = customStitch.displayName,
                    description = customStitch.description,
                    instructionsJson = json.encodeToString(customStitch.instructions),
                    stitchCount = customStitch.stitchCount,
                    rawDsl = customStitch.rawDsl,
                    sortOrder = index
                )
            )
        }

        // Insert components and rows
        pattern.components.forEachIndexed { componentIndex, component ->
            val componentId = componentDao.insertComponent(
                ComponentEntity(
                    patternId = patternId,
                    name = component.name,
                    sortOrder = componentIndex
                )
            )

            val rowEntities = component.rows.mapIndexed { rowIndex, row ->
                RowEntity(
                    componentId = componentId,
                    description = row.description,
                    sided = row.sided?.name,
                    hasMagicRing = row.hasMagicRing,
                    instructionsJson = json.encodeToString(row.instructions),
                    stitchCount = row.totalStitchCount,
                    sortOrder = rowIndex,
                    repeatCount = row.repeatCount
                )
            }
            rowDao.insertRows(rowEntities)
        }

        // Initialize progress if new pattern
        if (pattern.id == 0L) {
            val firstComponentId = componentDao.getComponentsForPattern(patternId)
            progressDao.upsertProgress(
                ProgressEntity(
                    patternId = patternId,
                    currentComponentId = null, // Will be set when components are loaded
                    currentRowIndex = 0,
                    currentStitchCount = 0
                )
            )
        }

        return patternId
    }

    /**
     * Delete a pattern.
     */
    suspend fun deletePattern(patternId: Long) {
        patternDao.deletePatternById(patternId)
    }

    // Extension functions for entity-to-domain conversion

    private fun PatternEntity.toDomain(): Pattern {
        return Pattern(
            id = id,
            name = name,
            description = description,
            photoUri = photoUri,
            components = emptyList(), // Loaded separately
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun RowEntity.toDomain(): PatternRow {
        val instructions: List<StitchGroup> = try {
            json.decodeFromString(instructionsJson)
        } catch (e: Exception) {
            emptyList()
        }

        return PatternRow(
            id = id,
            description = description,
            instructions = instructions,
            sided = Sided.fromString(sided),
            hasMagicRing = hasMagicRing,
            repeatCount = repeatCount
        )
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

    private fun CustomStitchEntity.toDomain(): CustomStitchDefinition {
        val instructions: List<StitchInstruction> = try {
            json.decodeFromString(instructionsJson)
        } catch (e: Exception) {
            emptyList()
        }

        return CustomStitchDefinition(
            abbreviation = abbreviation,
            displayName = displayName,
            description = description,
            instructions = instructions,
            stitchCount = stitchCount,
            rawDsl = rawDsl
        )
    }
}
