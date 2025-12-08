package xyz.chrismiller.crochetti.ui.screen.patternlist

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
import xyz.chrismiller.crochetti.domain.model.Progress
import javax.inject.Inject

data class PatternListItem(
    val pattern: Pattern,
    val progress: Progress?,
    val totalRows: Int = 0,
    val completedRows: Int = 0
)

data class PatternListUiState(
    val patterns: List<PatternListItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PatternListViewModel @Inject constructor(
    private val patternRepository: PatternRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatternListUiState())
    val uiState: StateFlow<PatternListUiState> = _uiState.asStateFlow()

    init {
        loadPatterns()
    }

    private fun loadPatterns() {
        viewModelScope.launch {
            patternRepository.getAllPatternsWithProgress()
                .collect { patternsWithProgress ->
                    val items = patternsWithProgress.map { (pattern, progress) ->
                        PatternListItem(
                            pattern = pattern,
                            progress = progress,
                            totalRows = pattern.totalRows,
                            completedRows = progress?.completedRowIds?.size ?: 0
                        )
                    }
                    _uiState.update {
                        it.copy(
                            patterns = items,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun deletePattern(patternId: Long) {
        viewModelScope.launch {
            try {
                patternRepository.deletePattern(patternId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to delete pattern: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
