package xyz.chrismiller.crochetti.ui.screen.patternprogress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.chrismiller.crochetti.data.repository.PatternRepository
import xyz.chrismiller.crochetti.data.repository.ProgressRepository
import xyz.chrismiller.crochetti.domain.model.ExpandedRow
import xyz.chrismiller.crochetti.domain.model.Pattern
import xyz.chrismiller.crochetti.domain.model.PatternComponent
import xyz.chrismiller.crochetti.domain.model.PatternRow
import xyz.chrismiller.crochetti.domain.model.Progress
import javax.inject.Inject

data class PatternProgressUiState(
    val pattern: Pattern? = null,
    val progress: Progress? = null,
    val currentComponent: PatternComponent? = null,
    val expandedRows: List<ExpandedRow> = emptyList(),
    val currentExpandedRow: ExpandedRow? = null,
    val currentVirtualRowIndex: Int = 0,
    val totalVirtualRowsInComponent: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val currentStitchCount: Int get() = progress?.currentStitchCount ?: 0
    val totalStitchesInRow: Int get() = currentExpandedRow?.sourceRow?.totalStitchCount ?: 0
    val isRowComplete: Boolean get() = currentStitchCount >= totalStitchesInRow && totalStitchesInRow > 0

    // For backward compatibility
    val currentRow: PatternRow? get() = currentExpandedRow?.sourceRow
    val currentRowNumber: Int get() = currentVirtualRowIndex + 1
    val totalRowsInComponent: Int get() = totalVirtualRowsInComponent
}

@HiltViewModel
class PatternProgressViewModel @Inject constructor(
    private val patternRepository: PatternRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatternProgressUiState())
    val uiState: StateFlow<PatternProgressUiState> = _uiState.asStateFlow()

    fun loadPattern(patternId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Combine pattern and progress flows
                combine(
                    patternRepository.getPatternWithDetailsFlow(patternId),
                    progressRepository.getProgress(patternId)
                ) { pattern, progress ->
                    Pair(pattern, progress)
                }.collect { (pattern, progress) ->
                    if (pattern == null) {
                        _uiState.update {
                            it.copy(isLoading = false, error = "Pattern not found")
                        }
                        return@collect
                    }

                    // Initialize progress if needed
                    var currentProgress = progress
                    if (currentProgress == null) {
                        val firstComponent = pattern.components.firstOrNull()
                        progressRepository.initializeProgress(patternId, firstComponent?.id)
                        currentProgress = Progress.forPattern(patternId, firstComponent?.id)
                    }

                    // Find current component
                    val currentComponent = pattern.components.find {
                        it.id == currentProgress.currentComponentId
                    } ?: pattern.components.firstOrNull()

                    // Expand rows into virtual rows (accounting for repeats)
                    val expandedRows = currentComponent?.expandRows() ?: emptyList()

                    // Find current virtual row
                    val virtualRowIndex = currentProgress.currentRowIndex.coerceIn(
                        0,
                        (expandedRows.size - 1).coerceAtLeast(0)
                    )
                    val currentExpandedRow = expandedRows.getOrNull(virtualRowIndex)

                    _uiState.update {
                        it.copy(
                            pattern = pattern,
                            progress = currentProgress,
                            currentComponent = currentComponent,
                            expandedRows = expandedRows,
                            currentExpandedRow = currentExpandedRow,
                            currentVirtualRowIndex = virtualRowIndex,
                            totalVirtualRowsInComponent = expandedRows.size,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load: ${e.message}")
                }
            }
        }
    }

    fun incrementStitch() {
        val state = _uiState.value
        val patternId = state.pattern?.id ?: return
        val maxStitches = state.totalStitchesInRow

        // Allow going slightly over (common in crochet counting)
        if (state.currentStitchCount < maxStitches + 5) {
            viewModelScope.launch {
                progressRepository.incrementStitch(patternId)
            }
        }
    }

    fun decrementStitch() {
        val state = _uiState.value
        val patternId = state.pattern?.id ?: return

        if (state.currentStitchCount > 0) {
            viewModelScope.launch {
                progressRepository.decrementStitch(patternId)
            }
        }
    }

    fun setStitchCount(count: Int) {
        val state = _uiState.value
        val progress = state.progress ?: return
        val patternId = state.pattern?.id ?: return

        viewModelScope.launch {
            progressRepository.updateProgress(
                progress.copy(
                    currentStitchCount = count.coerceAtLeast(0),
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    fun completeRow() {
        val state = _uiState.value
        val patternId = state.pattern?.id ?: return
        val currentExpandedRow = state.currentExpandedRow ?: return

        val nextVirtualRowIndex = state.currentVirtualRowIndex + 1
        val isLastVirtualRow = nextVirtualRowIndex >= state.expandedRows.size

        viewModelScope.launch {
            if (isLastVirtualRow) {
                // Stay on last virtual row but mark complete
                progressRepository.completeRowAndAdvance(
                    patternId,
                    currentExpandedRow.sourceRow.id,
                    state.expandedRows.size - 1
                )
            } else {
                progressRepository.completeRowAndAdvance(
                    patternId,
                    currentExpandedRow.sourceRow.id,
                    nextVirtualRowIndex
                )
            }
        }
    }

    fun goToRow(virtualRowIndex: Int) {
        val state = _uiState.value
        val patternId = state.pattern?.id ?: return

        val safeIndex = virtualRowIndex.coerceIn(0, (state.expandedRows.size - 1).coerceAtLeast(0))

        viewModelScope.launch {
            progressRepository.advanceToRow(patternId, safeIndex)
        }
    }

    fun previousRow() {
        val currentIndex = _uiState.value.currentVirtualRowIndex
        if (currentIndex > 0) {
            goToRow(currentIndex - 1)
        }
    }

    fun nextRow() {
        val state = _uiState.value
        val currentIndex = state.currentVirtualRowIndex
        val maxIndex = (state.expandedRows.size - 1).coerceAtLeast(0)
        if (currentIndex < maxIndex) {
            goToRow(currentIndex + 1)
        }
    }

    fun selectComponent(componentId: Long) {
        val patternId = _uiState.value.pattern?.id ?: return

        viewModelScope.launch {
            progressRepository.selectComponent(patternId, componentId)
        }
    }

    fun resetProgress() {
        val state = _uiState.value
        val patternId = state.pattern?.id ?: return
        val firstComponentId = state.pattern?.components?.firstOrNull()?.id

        viewModelScope.launch {
            progressRepository.resetProgress(patternId, firstComponentId)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
