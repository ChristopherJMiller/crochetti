package xyz.chrismiller.crochetti.ui.screen.patternedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.chrismiller.crochetti.data.repository.PatternRepository
import xyz.chrismiller.crochetti.domain.model.CustomStitchDefinition
import xyz.chrismiller.crochetti.domain.model.Pattern
import xyz.chrismiller.crochetti.domain.model.PatternComponent
import xyz.chrismiller.crochetti.domain.model.PatternRow
import xyz.chrismiller.crochetti.domain.model.Sided
import xyz.chrismiller.crochetti.domain.model.Stitch
import xyz.chrismiller.crochetti.domain.model.StitchGroup
import xyz.chrismiller.crochetti.domain.parser.CustomStitchDslParser
import xyz.chrismiller.crochetti.domain.parser.PatternDslParser
import javax.inject.Inject

data class RowEditState(
    val dslText: String = "",
    val description: String = "",
    val sided: Sided? = Sided.RightSide,
    val hasMagicRing: Boolean = false,
    val parseResult: PatternDslParser.ParseResult? = null,
    val repeatCount: Int = 1 // 1 = single row, >1 = repeat rows (e.g., "7-9)")
)

data class ComponentEditState(
    val name: String = "Main",
    val rows: List<RowEditState> = listOf(RowEditState())
)

data class CustomStitchEditState(
    val abbreviation: String = "",
    val displayName: String = "",
    val description: String = "",
    val dslText: String = "",
    val parseResult: CustomStitchDslParser.ParseResult? = null,
    val isEditing: Boolean = false,
    val originalAbbreviation: String? = null
)

data class PatternEditUiState(
    val name: String = "",
    val description: String = "",
    val photoUri: String? = null,
    val components: List<ComponentEditState> = listOf(ComponentEditState()),
    val selectedComponentIndex: Int = 0,
    val customStitches: List<CustomStitchDefinition> = emptyList(),
    val editingCustomStitch: CustomStitchEditState? = null,
    val showCustomStitchEditor: Boolean = false,
    val showCustomStitchSection: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedPatternId: Long? = null,
    val showAddRowFab: Boolean = false
)

@HiltViewModel
class PatternEditViewModel @Inject constructor(
    private val patternRepository: PatternRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatternEditUiState())
    val uiState: StateFlow<PatternEditUiState> = _uiState.asStateFlow()

    /**
     * Get a parser that includes the current custom stitches.
     */
    private fun getParser(): PatternDslParser {
        val customStitchMap = _uiState.value.customStitches.associate {
            it.normalizedAbbreviation to it.toStitch()
        }
        return PatternDslParser(customStitchMap)
    }

    fun loadPattern(patternId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val pattern = patternRepository.getPatternWithDetails(patternId)
                if (pattern != null) {
                    // First set custom stitches so parser can use them
                    val customStitches = pattern.customStitches

                    // Create parser with custom stitches
                    val customStitchMap = customStitches.associate {
                        it.normalizedAbbreviation to it.toStitch()
                    }
                    val parser = PatternDslParser(customStitchMap)

                    val components = pattern.components.map { component ->
                        ComponentEditState(
                            name = component.name,
                            rows = component.rows.map { row ->
                                val dslText = row.instructions.joinToString(", ") {
                                    it.toDisplayString()
                                }
                                RowEditState(
                                    dslText = dslText,
                                    description = row.description,
                                    sided = row.sided,
                                    hasMagicRing = row.hasMagicRing,
                                    parseResult = parser.parse(dslText),
                                    repeatCount = row.repeatCount
                                )
                            }.ifEmpty { listOf(RowEditState()) }
                        )
                    }.ifEmpty { listOf(ComponentEditState()) }

                    _uiState.update {
                        it.copy(
                            name = pattern.name,
                            description = pattern.description,
                            photoUri = pattern.photoUri,
                            components = components,
                            customStitches = customStitches,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load pattern: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updatePhotoUri(uri: String?) {
        _uiState.update { it.copy(photoUri = uri) }
    }

    fun selectComponent(index: Int) {
        _uiState.update { it.copy(selectedComponentIndex = index) }
    }

    fun updateComponentName(index: Int, name: String) {
        _uiState.update { state ->
            val components = state.components.toMutableList()
            if (index in components.indices) {
                components[index] = components[index].copy(name = name)
            }
            state.copy(components = components)
        }
    }

    fun addComponent() {
        _uiState.update { state ->
            val newComponent = ComponentEditState(
                name = "Component ${state.components.size + 1}"
            )
            state.copy(
                components = state.components + newComponent,
                selectedComponentIndex = state.components.size
            )
        }
    }

    fun removeComponent(index: Int) {
        _uiState.update { state ->
            if (state.components.size <= 1) return@update state

            val components = state.components.toMutableList()
            components.removeAt(index)
            state.copy(
                components = components,
                selectedComponentIndex = minOf(state.selectedComponentIndex, components.size - 1)
            )
        }
    }

    fun updateRowDsl(componentIndex: Int, rowIndex: Int, dslText: String) {
        _uiState.update { state ->
            val components = state.components.toMutableList()
            if (componentIndex in components.indices) {
                val component = components[componentIndex]
                val rows = component.rows.toMutableList()
                if (rowIndex in rows.indices) {
                    val parseResult = getParser().parse(dslText)
                    rows[rowIndex] = rows[rowIndex].copy(
                        dslText = dslText,
                        parseResult = parseResult
                    )
                    components[componentIndex] = component.copy(rows = rows)
                }
            }
            state.copy(components = components)
        }
    }

    fun updateRowDescription(componentIndex: Int, rowIndex: Int, description: String) {
        _uiState.update { state ->
            val components = state.components.toMutableList()
            if (componentIndex in components.indices) {
                val component = components[componentIndex]
                val rows = component.rows.toMutableList()
                if (rowIndex in rows.indices) {
                    rows[rowIndex] = rows[rowIndex].copy(description = description)
                    components[componentIndex] = component.copy(rows = rows)
                }
            }
            state.copy(components = components)
        }
    }

    fun updateRowSided(componentIndex: Int, rowIndex: Int, sided: Sided?) {
        _uiState.update { state ->
            val components = state.components.toMutableList()
            if (componentIndex in components.indices) {
                val component = components[componentIndex]
                val rows = component.rows.toMutableList()
                if (rowIndex in rows.indices) {
                    rows[rowIndex] = rows[rowIndex].copy(sided = sided)
                    components[componentIndex] = component.copy(rows = rows)
                }
            }
            state.copy(components = components)
        }
    }

    fun updateRowMagicRing(componentIndex: Int, rowIndex: Int, hasMagicRing: Boolean) {
        _uiState.update { state ->
            val components = state.components.toMutableList()
            if (componentIndex in components.indices) {
                val component = components[componentIndex]
                val rows = component.rows.toMutableList()
                if (rowIndex in rows.indices) {
                    rows[rowIndex] = rows[rowIndex].copy(hasMagicRing = hasMagicRing)
                    components[componentIndex] = component.copy(rows = rows)
                }
            }
            state.copy(components = components)
        }
    }

    fun updateRowRepeatCount(componentIndex: Int, rowIndex: Int, repeatCount: Int) {
        _uiState.update { state ->
            val components = state.components.toMutableList()
            if (componentIndex in components.indices) {
                val component = components[componentIndex]
                val rows = component.rows.toMutableList()
                if (rowIndex in rows.indices) {
                    rows[rowIndex] = rows[rowIndex].copy(
                        repeatCount = repeatCount.coerceIn(1, 99)
                    )
                    components[componentIndex] = component.copy(rows = rows)
                }
            }
            state.copy(components = components)
        }
    }

    fun addRow(componentIndex: Int) {
        _uiState.update { state ->
            val components = state.components.toMutableList()
            if (componentIndex in components.indices) {
                val component = components[componentIndex]
                components[componentIndex] = component.copy(
                    rows = component.rows + RowEditState()
                )
            }
            state.copy(components = components)
        }
    }

    fun removeRow(componentIndex: Int, rowIndex: Int) {
        _uiState.update { state ->
            val components = state.components.toMutableList()
            if (componentIndex in components.indices) {
                val component = components[componentIndex]
                if (component.rows.size <= 1) return@update state

                val rows = component.rows.toMutableList()
                rows.removeAt(rowIndex)
                components[componentIndex] = component.copy(rows = rows)
            }
            state.copy(components = components)
        }
    }

    fun savePattern(existingPatternId: Long?) {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Pattern name is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val components = state.components.map { componentState ->
                    PatternComponent(
                        name = componentState.name,
                        rows = componentState.rows.mapIndexedNotNull { rowIndex, rowState ->
                            val parseResult = rowState.parseResult
                            if (parseResult is PatternDslParser.ParseResult.Success &&
                                parseResult.groups.isNotEmpty()
                            ) {
                                PatternRow(
                                    description = rowState.description,
                                    instructions = parseResult.groups,
                                    sided = rowState.sided,
                                    hasMagicRing = rowIndex == 0 && rowState.hasMagicRing,
                                    repeatCount = rowState.repeatCount
                                )
                            } else if (rowState.dslText.isBlank()) {
                                null // Skip empty rows
                            } else {
                                // Include row even if parse failed, with empty instructions
                                PatternRow(
                                    description = rowState.description,
                                    instructions = emptyList(),
                                    sided = rowState.sided,
                                    hasMagicRing = rowIndex == 0 && rowState.hasMagicRing,
                                    repeatCount = rowState.repeatCount
                                )
                            }
                        }
                    )
                }.filter { it.rows.isNotEmpty() }

                val pattern = Pattern(
                    id = existingPatternId ?: 0,
                    name = state.name,
                    description = state.description,
                    photoUri = state.photoUri,
                    components = components.ifEmpty {
                        listOf(PatternComponent(name = "Main", rows = emptyList()))
                    },
                    customStitches = state.customStitches
                )

                val savedId = patternRepository.savePattern(pattern)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savedPatternId = savedId
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save pattern: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun updateShowAddRowFab(show: Boolean) {
        _uiState.update { it.copy(showAddRowFab = show) }
    }

    // ==================== CUSTOM STITCH METHODS ====================

    fun toggleCustomStitchSection() {
        _uiState.update { it.copy(showCustomStitchSection = !it.showCustomStitchSection) }
    }

    fun startAddCustomStitch() {
        _uiState.update {
            it.copy(
                editingCustomStitch = CustomStitchEditState(),
                showCustomStitchEditor = true
            )
        }
    }

    fun startEditCustomStitch(definition: CustomStitchDefinition) {
        _uiState.update {
            it.copy(
                editingCustomStitch = CustomStitchEditState(
                    abbreviation = definition.abbreviation,
                    displayName = definition.displayName,
                    description = definition.description,
                    dslText = definition.rawDsl,
                    parseResult = CustomStitchDslParser.parse(
                        definition.rawDsl,
                        definition.abbreviation,
                        definition.displayName,
                        definition.description,
                        getExistingDefinitionsExcluding(definition.abbreviation)
                    ),
                    isEditing = true,
                    originalAbbreviation = definition.abbreviation
                ),
                showCustomStitchEditor = true
            )
        }
    }

    fun cancelCustomStitchEdit() {
        _uiState.update {
            it.copy(
                editingCustomStitch = null,
                showCustomStitchEditor = false
            )
        }
    }

    fun updateCustomStitchAbbreviation(abbreviation: String) {
        _uiState.update { state ->
            val editing = state.editingCustomStitch ?: return@update state
            val newEditing = editing.copy(abbreviation = abbreviation)
            state.copy(editingCustomStitch = reparseCustomStitch(newEditing))
        }
    }

    fun updateCustomStitchDisplayName(displayName: String) {
        _uiState.update { state ->
            val editing = state.editingCustomStitch ?: return@update state
            val newEditing = editing.copy(displayName = displayName)
            state.copy(editingCustomStitch = reparseCustomStitch(newEditing))
        }
    }

    fun updateCustomStitchDescription(description: String) {
        _uiState.update { state ->
            val editing = state.editingCustomStitch ?: return@update state
            state.copy(editingCustomStitch = editing.copy(description = description))
        }
    }

    fun updateCustomStitchDsl(dslText: String) {
        _uiState.update { state ->
            val editing = state.editingCustomStitch ?: return@update state
            val newEditing = editing.copy(dslText = dslText)
            state.copy(editingCustomStitch = reparseCustomStitch(newEditing))
        }
    }

    private fun reparseCustomStitch(editing: CustomStitchEditState): CustomStitchEditState {
        if (editing.abbreviation.isBlank() || editing.displayName.isBlank() || editing.dslText.isBlank()) {
            return editing.copy(parseResult = null)
        }

        val excludeAbbr = if (editing.isEditing) editing.originalAbbreviation else null
        val parseResult = CustomStitchDslParser.parse(
            editing.dslText,
            editing.abbreviation,
            editing.displayName,
            editing.description,
            getExistingDefinitionsExcluding(excludeAbbr)
        )
        return editing.copy(parseResult = parseResult)
    }

    private fun getExistingDefinitionsExcluding(abbreviation: String?): Map<String, CustomStitchDefinition> {
        return _uiState.value.customStitches
            .filter { it.normalizedAbbreviation != abbreviation?.lowercase()?.trim() }
            .associateBy { it.normalizedAbbreviation }
    }

    fun saveCustomStitch() {
        val state = _uiState.value
        val editing = state.editingCustomStitch ?: return
        val parseResult = editing.parseResult

        if (parseResult !is CustomStitchDslParser.ParseResult.Success) {
            _uiState.update { it.copy(error = "Invalid custom stitch definition") }
            return
        }

        val newDefinition = parseResult.definition

        // Check for duplicate abbreviation (excluding the one being edited)
        val isDuplicate = state.customStitches.any { existing ->
            existing.normalizedAbbreviation == newDefinition.normalizedAbbreviation &&
                    existing.normalizedAbbreviation != editing.originalAbbreviation?.lowercase()?.trim()
        }

        if (isDuplicate) {
            _uiState.update { it.copy(error = "A custom stitch with abbreviation '${newDefinition.abbreviation}' already exists") }
            return
        }

        _uiState.update { uiState ->
            val updatedStitches = if (editing.isEditing) {
                uiState.customStitches.map {
                    if (it.normalizedAbbreviation == editing.originalAbbreviation?.lowercase()?.trim()) {
                        newDefinition
                    } else {
                        it
                    }
                }
            } else {
                uiState.customStitches + newDefinition
            }

            // Re-parse all row DSLs since custom stitches have changed
            val updatedComponents = reparseAllRows(uiState.components, updatedStitches)

            uiState.copy(
                customStitches = updatedStitches,
                components = updatedComponents,
                editingCustomStitch = null,
                showCustomStitchEditor = false
            )
        }
    }

    fun deleteCustomStitch(definition: CustomStitchDefinition) {
        _uiState.update { state ->
            val updatedStitches = state.customStitches.filter {
                it.normalizedAbbreviation != definition.normalizedAbbreviation
            }

            // Re-parse all row DSLs since custom stitches have changed
            val updatedComponents = reparseAllRows(state.components, updatedStitches)

            state.copy(
                customStitches = updatedStitches,
                components = updatedComponents
            )
        }
    }

    private fun reparseAllRows(
        components: List<ComponentEditState>,
        customStitches: List<CustomStitchDefinition>
    ): List<ComponentEditState> {
        val customStitchMap = customStitches.associate {
            it.normalizedAbbreviation to it.toStitch()
        }
        val parser = PatternDslParser(customStitchMap)

        return components.map { component ->
            component.copy(
                rows = component.rows.map { row ->
                    row.copy(parseResult = parser.parse(row.dslText))
                }
            )
        }
    }
}
