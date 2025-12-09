package xyz.chrismiller.crochetti.ui.screen.projectprogress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.chrismiller.crochetti.data.repository.PatternRepository
import xyz.chrismiller.crochetti.data.repository.ProjectRepository
import xyz.chrismiller.crochetti.domain.model.ExpandedRow
import xyz.chrismiller.crochetti.domain.model.Pattern
import xyz.chrismiller.crochetti.domain.model.PatternComponent
import xyz.chrismiller.crochetti.domain.model.PatternRow
import xyz.chrismiller.crochetti.domain.model.Project
import xyz.chrismiller.crochetti.domain.model.AdjacentStitchInfo
import xyz.chrismiller.crochetti.domain.model.StitchPosition
import xyz.chrismiller.crochetti.domain.model.deriveStitchPosition
import xyz.chrismiller.crochetti.ui.navigation.Screen
import javax.inject.Inject

data class ProjectProgressUiState(
    val project: Project? = null,
    val pattern: Pattern? = null,
    val currentComponent: PatternComponent? = null,
    val expandedRows: List<ExpandedRow> = emptyList(),
    val currentExpandedRow: ExpandedRow? = null,
    val currentVirtualRowIndex: Int = 0,
    val totalVirtualRowsInComponent: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val currentStitchCount: Int get() = project?.currentStitchCount ?: 0
    val totalStitchesInRow: Int get() = currentExpandedRow?.sourceRow?.totalStitchCount ?: 0
    val isRowComplete: Boolean get() = currentStitchCount >= totalStitchesInRow && totalStitchesInRow > 0

    // For backward compatibility
    val currentRow: PatternRow? get() = currentExpandedRow?.sourceRow
    val currentRowNumber: Int get() = currentVirtualRowIndex + 1
    val totalRowsInComponent: Int get() = totalVirtualRowsInComponent

    // Derived stitch position for the indicator
    val currentStitchPosition: StitchPosition?
        get() = currentRow?.instructions?.let { instructions ->
            deriveStitchPosition(currentStitchCount, instructions)
        }

    // First stitch info for when count is 0
    val firstStitchInfo: AdjacentStitchInfo?
        get() {
            val instructions = currentRow?.instructions ?: return null
            if (instructions.isEmpty()) return null
            val firstGroup = instructions.first()
            if (firstGroup.stitches.isEmpty()) return null

            // Count consecutive first stitches
            val firstStitch = firstGroup.stitches.first()
            var count = 1
            for (i in 1 until firstGroup.stitches.size) {
                if (firstGroup.stitches[i] == firstStitch) count++ else break
            }

            return AdjacentStitchInfo(
                stitch = firstStitch,
                consecutiveCount = count,
                isInRepeatingGroup = firstGroup.repetitions > 1,
                groupRepetitions = firstGroup.repetitions
            )
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProjectProgressViewModel @Inject constructor(
    private val patternRepository: PatternRepository,
    private val projectRepository: ProjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val projectId: Long = savedStateHandle.get<Long>(Screen.PROJECT_ID_ARG) ?: 0

    private val _uiState = MutableStateFlow(ProjectProgressUiState())
    val uiState: StateFlow<ProjectProgressUiState> = _uiState.asStateFlow()

    init {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                projectRepository.getProject(projectId)
                    .filterNotNull()
                    .flatMapLatest { project ->
                        patternRepository.getPatternWithDetailsFlow(project.patternId)
                            .filterNotNull()
                            .combine(kotlinx.coroutines.flow.flowOf(project)) { pattern, proj ->
                                Pair(pattern, proj)
                            }
                    }
                    .collect { (pattern, project) ->
                        // Find current component
                        val currentComponent = pattern.components.find {
                            it.id == project.currentComponentId
                        } ?: pattern.components.firstOrNull()

                        // Expand rows into virtual rows (accounting for repeats)
                        val expandedRows = currentComponent?.expandRows() ?: emptyList()

                        // Find current virtual row
                        val virtualRowIndex = project.currentRowIndex.coerceIn(
                            0,
                            (expandedRows.size - 1).coerceAtLeast(0)
                        )
                        val currentExpandedRow = expandedRows.getOrNull(virtualRowIndex)

                        _uiState.update {
                            it.copy(
                                project = project,
                                pattern = pattern,
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
        val maxStitches = state.totalStitchesInRow

        // Allow going slightly over (common in crochet counting)
        if (state.currentStitchCount < maxStitches + 5) {
            viewModelScope.launch {
                projectRepository.incrementStitch(projectId)
            }
        }
    }

    fun decrementStitch() {
        val state = _uiState.value

        if (state.currentStitchCount > 0) {
            viewModelScope.launch {
                projectRepository.decrementStitch(projectId)
            }
        }
    }

    fun setStitchCount(count: Int) {
        val state = _uiState.value
        val project = state.project ?: return

        viewModelScope.launch {
            projectRepository.updateProject(
                project.copy(
                    currentStitchCount = count.coerceAtLeast(0),
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    fun completeRow() {
        val state = _uiState.value
        val currentExpandedRow = state.currentExpandedRow ?: return

        val nextVirtualRowIndex = state.currentVirtualRowIndex + 1
        val isLastVirtualRow = nextVirtualRowIndex >= state.expandedRows.size

        viewModelScope.launch {
            if (isLastVirtualRow) {
                // Stay on last virtual row but mark complete
                projectRepository.completeRowAndAdvance(
                    projectId,
                    currentExpandedRow.progressKey,
                    state.expandedRows.size - 1
                )
            } else {
                projectRepository.completeRowAndAdvance(
                    projectId,
                    currentExpandedRow.progressKey,
                    nextVirtualRowIndex
                )
            }
        }
    }

    fun goToRow(virtualRowIndex: Int) {
        val state = _uiState.value

        val safeIndex = virtualRowIndex.coerceIn(0, (state.expandedRows.size - 1).coerceAtLeast(0))

        viewModelScope.launch {
            projectRepository.advanceToRow(projectId, safeIndex)
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
        viewModelScope.launch {
            projectRepository.selectComponent(projectId, componentId)
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            projectRepository.resetProject(projectId)
        }
    }

    fun markComplete() {
        viewModelScope.launch {
            projectRepository.markProjectCompleted(projectId)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
