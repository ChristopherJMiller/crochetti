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
import xyz.chrismiller.crochetti.data.repository.ProjectRepository
import xyz.chrismiller.crochetti.domain.model.Pattern
import javax.inject.Inject

data class PatternListItem(
    val pattern: Pattern,
    val activeProjectCount: Int = 0,
    val completedProjectCount: Int = 0,
    val totalRows: Int = 0
)

data class PatternListUiState(
    val patterns: List<PatternListItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PatternListViewModel @Inject constructor(
    private val patternRepository: PatternRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatternListUiState())
    val uiState: StateFlow<PatternListUiState> = _uiState.asStateFlow()

    init {
        loadPatterns()
    }

    private fun loadPatterns() {
        viewModelScope.launch {
            patternRepository.getAllPatternsWithProjectCounts()
                .collect { patternsWithCounts ->
                    val items = patternsWithCounts.map { patternWithCounts ->
                        PatternListItem(
                            pattern = patternWithCounts.pattern,
                            activeProjectCount = patternWithCounts.activeProjectCount,
                            completedProjectCount = patternWithCounts.completedProjectCount,
                            totalRows = patternWithCounts.pattern.totalRows
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

    suspend fun getOrCreateProjectForPattern(patternId: Long): Long {
        val existingProjects = projectRepository.getProjectsForPatternSync(patternId)
        return if (existingProjects.isEmpty()) {
            projectRepository.createProject(patternId)
        } else {
            existingProjects.first().id
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
