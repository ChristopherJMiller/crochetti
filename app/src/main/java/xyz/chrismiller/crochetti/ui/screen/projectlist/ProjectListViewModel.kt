package xyz.chrismiller.crochetti.ui.screen.projectlist

import androidx.lifecycle.SavedStateHandle
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
import xyz.chrismiller.crochetti.data.repository.ProjectRepository
import xyz.chrismiller.crochetti.domain.model.Pattern
import xyz.chrismiller.crochetti.domain.model.Project
import xyz.chrismiller.crochetti.ui.navigation.Screen
import javax.inject.Inject

data class ProjectListItem(
    val project: Project,
    val totalRows: Int = 0,
    val completedRows: Int = 0
)

data class ProjectListUiState(
    val patternId: Long = 0,
    val patternName: String = "",
    val pattern: Pattern? = null,
    val projects: List<ProjectListItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showShareSheet: Boolean = false
)

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val patternRepository: PatternRepository,
    private val projectRepository: ProjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val patternId: Long = savedStateHandle.get<Long>(Screen.PATTERN_ID_ARG) ?: 0

    private val _uiState = MutableStateFlow(ProjectListUiState(patternId = patternId))
    val uiState: StateFlow<ProjectListUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                patternRepository.getPatternWithDetailsFlow(patternId),
                projectRepository.getProjectsForPattern(patternId)
            ) { pattern: Pattern?, projects: List<Project> ->
                pattern to projects
            }.collect { (pattern, projects) ->
                if (pattern != null) {
                    // Use virtual rows (accounting for repeats) for accurate progress
                    val totalVirtualRows = pattern.totalVirtualRows
                    val items = projects.map { project ->
                        // Use current position (row index + 1) for progress
                        val currentPosition = project.currentRowIndex + 1
                        ProjectListItem(
                            project = project,
                            totalRows = totalVirtualRows,
                            completedRows = currentPosition.coerceAtMost(totalVirtualRows)
                        )
                    }
                    _uiState.update {
                        it.copy(
                            patternName = pattern.name,
                            pattern = pattern,
                            projects = items,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun createNewProject() {
        viewModelScope.launch {
            try {
                projectRepository.createProject(patternId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to create project: ${e.message}")
                }
            }
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            try {
                projectRepository.deleteProject(projectId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to delete project: ${e.message}")
                }
            }
        }
    }

    fun markProjectComplete(projectId: Long) {
        viewModelScope.launch {
            try {
                projectRepository.markProjectCompleted(projectId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to mark project complete: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun showShareSheet() {
        _uiState.update { it.copy(showShareSheet = true) }
    }

    fun hideShareSheet() {
        _uiState.update { it.copy(showShareSheet = false) }
    }
}
