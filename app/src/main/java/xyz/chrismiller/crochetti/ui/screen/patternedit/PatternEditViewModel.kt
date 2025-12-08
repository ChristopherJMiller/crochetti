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
import xyz.chrismiller.crochetti.domain.model.Pattern
import xyz.chrismiller.crochetti.domain.model.PatternComponent
import xyz.chrismiller.crochetti.domain.model.PatternRow
import xyz.chrismiller.crochetti.domain.model.Sided
import xyz.chrismiller.crochetti.domain.model.StitchGroup
import xyz.chrismiller.crochetti.domain.parser.PatternDslParser
import javax.inject.Inject

data class RowEditState(
    val dslText: String = "",
    val description: String = "",
    val sided: Sided? = Sided.RightSide,
    val hasMagicRing: Boolean = false,
    val parseResult: PatternDslParser.ParseResult? = null
)

data class ComponentEditState(
    val name: String = "Main",
    val rows: List<RowEditState> = listOf(RowEditState())
)

data class PatternEditUiState(
    val name: String = "",
    val description: String = "",
    val photoUri: String? = null,
    val components: List<ComponentEditState> = listOf(ComponentEditState()),
    val selectedComponentIndex: Int = 0,
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

    private val parser = PatternDslParser()

    fun loadPattern(patternId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val pattern = patternRepository.getPatternWithDetails(patternId)
                if (pattern != null) {
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
                                    parseResult = parser.parse(dslText)
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
                    val parseResult = parser.parse(dslText)
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
                                    hasMagicRing = rowIndex == 0 && rowState.hasMagicRing
                                )
                            } else if (rowState.dslText.isBlank()) {
                                null // Skip empty rows
                            } else {
                                // Include row even if parse failed, with empty instructions
                                PatternRow(
                                    description = rowState.description,
                                    instructions = emptyList(),
                                    sided = rowState.sided,
                                    hasMagicRing = rowIndex == 0 && rowState.hasMagicRing
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
                    }
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
}
