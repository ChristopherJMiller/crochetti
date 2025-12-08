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
import xyz.chrismiller.crochetti.domain.model.Pattern
import xyz.chrismiller.crochetti.domain.model.PatternComponent
import xyz.chrismiller.crochetti.domain.model.PatternRow
import xyz.chrismiller.crochetti.domain.model.Progress
import javax.inject.Inject

data class PatternProgressUiState(
    val pattern: Pattern? = null,
    val progress: Progress? = null,
    val currentComponent: PatternComponent? = null,
    val currentRow: PatternRow? = null,
    val currentRowNumber: Int = 1,
    val totalRowsInComponent: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val currentStitchCount: Int get() = progress?.currentStitchCount ?: 0
    val totalStitchesInRow: Int get() = currentRow?.totalStitchCount ?: 0
    val isRowComplete: Boolean get() = currentStitchCount >= totalStitchesInRow && totalStitchesInRow > 0
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

                    // Find current row
                    val rowIndex = currentProgress.currentRowIndex.coerceIn(
                        0,
                        (currentComponent?.rows?.size ?: 1) - 1
                    )
                    val currentRow = currentComponent?.rows?.getOrNull(rowIndex)

                    _uiState.update {
                        it.copy(
                            pattern = pattern,
                            progress = currentProgress,
                            currentComponent = currentComponent,
                            currentRow = currentRow,
                            currentRowNumber = rowIndex + 1,
                            totalRowsInComponent = currentComponent?.rows?.size ?: 0,
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
        val currentRow = state.currentRow ?: return
        val currentComponent = state.currentComponent ?: return

        val nextRowIndex = state.progress?.currentRowIndex?.plus(1) ?: 0
        val isLastRow = nextRowIndex >= currentComponent.rows.size

        viewModelScope.launch {
            if (isLastRow) {
                // Stay on last row but mark complete
                progressRepository.completeRowAndAdvance(
                    patternId,
                    currentRow.id,
                    currentComponent.rows.size - 1
                )
            } else {
                progressRepository.completeRowAndAdvance(
                    patternId,
                    currentRow.id,
                    nextRowIndex
                )
            }
        }
    }

    fun goToRow(rowIndex: Int) {
        val state = _uiState.value
        val patternId = state.pattern?.id ?: return
        val currentComponent = state.currentComponent ?: return

        val safeIndex = rowIndex.coerceIn(0, currentComponent.rows.size - 1)

        viewModelScope.launch {
            progressRepository.advanceToRow(patternId, safeIndex)
        }
    }

    fun previousRow() {
        val currentIndex = _uiState.value.progress?.currentRowIndex ?: 0
        if (currentIndex > 0) {
            goToRow(currentIndex - 1)
        }
    }

    fun nextRow() {
        val state = _uiState.value
        val currentIndex = state.progress?.currentRowIndex ?: 0
        val maxIndex = (state.currentComponent?.rows?.size ?: 1) - 1
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
