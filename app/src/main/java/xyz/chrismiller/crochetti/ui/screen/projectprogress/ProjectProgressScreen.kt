package xyz.chrismiller.crochetti.ui.screen.projectprogress

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import xyz.chrismiller.crochetti.ui.components.StitchHelpBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectProgressScreen(
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: ProjectProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showResetDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showStitchHelp by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Progress?") },
            text = { Text("This will reset your progress to the beginning. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetProgress()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Mark Project Complete?") },
            text = { Text("This will mark this project as completed. You can start a new project from the same pattern anytime.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.markComplete()
                        showCompleteDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Complete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Stitch Help Bottom Sheet
    if (showStitchHelp) {
        val currentRowStitches = uiState.currentRow?.instructions
            ?.flatMap { it.stitches }
            ?: emptyList()

        StitchHelpBottomSheet(
            onDismiss = { showStitchHelp = false },
            sheetState = sheetState,
            currentRowStitches = currentRowStitches,
            customStitches = uiState.pattern?.customStitches ?: emptyList(),
            hasMagicRing = uiState.currentRow?.hasMagicRing ?: false,
            sided = uiState.currentRow?.sided
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.project?.name ?: uiState.pattern?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showStitchHelp = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Stitch Guide"
                        )
                    }
                    IconButton(onClick = { showCompleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Mark Complete"
                        )
                    }
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Progress"
                        )
                    }
                    IconButton(onClick = { uiState.pattern?.id?.let { onEditClick(it) } }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Pattern"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            ProgressContent(
                uiState = uiState,
                onIncrementStitch = viewModel::incrementStitch,
                onDecrementStitch = viewModel::decrementStitch,
                onCompleteRow = viewModel::completeRow,
                onPreviousRow = viewModel::previousRow,
                onNextRow = viewModel::nextRow,
                onSelectComponent = viewModel::selectComponent,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun ProgressContent(
    uiState: ProjectProgressUiState,
    onIncrementStitch: () -> Unit,
    onDecrementStitch: () -> Unit,
    onCompleteRow: () -> Unit,
    onPreviousRow: () -> Unit,
    onNextRow: () -> Unit,
    onSelectComponent: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Component selector (if multiple)
        val components = uiState.pattern?.components ?: emptyList()
        if (components.size > 1) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(components) { component ->
                    FilterChip(
                        selected = component.id == uiState.currentComponent?.id,
                        onClick = { onSelectComponent(component.id) },
                        label = { Text(component.name) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Row info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        // Row display: "Row 7" or "Row 7-9" for repeats
                        val expandedRow = uiState.currentExpandedRow
                        val rowLabel = if (expandedRow?.isPartOfRepeat == true) {
                            "Row ${expandedRow.repeatRangeLabel}"
                        } else {
                            "Row ${uiState.currentRowNumber}"
                        }
                        Text(
                            text = "$rowLabel of ${uiState.totalRowsInComponent}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        // Show repeat context: "(2 of 3)" for repeating rows
                        if (expandedRow?.isPartOfRepeat == true) {
                            Text(
                                text = "Repetition ${expandedRow.repetitionIndex + 1} of ${expandedRow.sourceRow.repeatCount}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    // Row indicators (RS/WS and Magic Ring)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        uiState.currentRow?.sided?.let { sided ->
                            Text(
                                text = sided.abbreviation,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (uiState.currentRow?.hasMagicRing == true) {
                            Text(
                                text = "MR",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row instructions (prepend MR if applicable)
                val instructionPrefix = if (uiState.currentRow?.hasMagicRing == true) "MR, " else ""
                Text(
                    text = instructionPrefix + (uiState.currentRow?.instructionsOnlyString() ?: "No instructions"),
                    style = MaterialTheme.typography.bodyLarge
                )

                // Total stitches
                Text(
                    text = "(${uiState.totalStitchesInRow} stitches)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Row description/notes
                uiState.currentRow?.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Counter button
        CounterButton(
            currentCount = uiState.currentStitchCount,
            totalCount = uiState.totalStitchesInRow,
            onIncrement = onIncrementStitch,
            onDecrement = onDecrementStitch
        )

        // Stitch position indicator
        Spacer(modifier = Modifier.height(16.dp))
        StitchPositionIndicator(
            position = uiState.currentStitchPosition,
            firstStitchInfo = uiState.firstStitchInfo
        )

        Spacer(modifier = Modifier.weight(1f))

        // Row navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPreviousRow,
                enabled = uiState.currentRowNumber > 1
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Prev")
            }

            Button(
                onClick = onCompleteRow,
                enabled = uiState.totalStitchesInRow > 0
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete Row")
            }

            OutlinedButton(
                onClick = onNextRow,
                enabled = uiState.currentRowNumber < uiState.totalRowsInComponent
            ) {
                Text("Next")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CounterButton(
    currentCount: Int,
    totalCount: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    val isComplete = currentCount >= totalCount && totalCount > 0
    val backgroundColor = if (isComplete) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (isComplete) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Box(
        modifier = modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onIncrement()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDecrement()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$currentCount",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = contentColor
            )
            Text(
                text = "of $totalCount",
                style = MaterialTheme.typography.titleMedium,
                color = contentColor.copy(alpha = 0.7f)
            )
            if (isComplete) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Row complete",
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Tap to count • Long press to undo",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun StitchPositionIndicator(
    position: xyz.chrismiller.crochetti.domain.model.StitchPosition?,
    firstStitchInfo: xyz.chrismiller.crochetti.domain.model.AdjacentStitchInfo?,
    modifier: Modifier = Modifier
) {
    // When count is 0, show the first stitch faded
    if (position == null) {
        if (firstStitchInfo != null) {
            Column(
                modifier = modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Next: ${formatStitchDisplay(firstStitchInfo.stitch, firstStitchInfo.consecutiveCount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous stitch (faded)
            if (position.previousStitch != null) {
                Text(
                    text = formatStitchDisplay(
                        position.previousStitch.stitch,
                        position.previousStitch.consecutiveCount
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(end = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Current stitch (highlighted)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Stitch name with position if consecutive
                        if (position.isInConsecutiveRun) {
                            Text(
                                text = "${position.currentStitch.abbreviation} ${position.positionInConsecutive}/${position.consecutiveCount}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Text(
                                text = position.currentStitch.abbreviation,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Sub-stitch dots for multi-count stitches (like inc)
                        if (position.hasMultipleSubStitches) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(position.totalSubStitches) { index ->
                                    val isFilled = index < position.subStitchProgress
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isFilled) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                // Group repetition indicator
                if (position.isInRepeatingGroup) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "x${position.groupRepetition}/${position.totalGroupRepetitions}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Next stitch (faded)
            if (position.nextStitch != null) {
                Text(
                    text = formatStitchDisplay(
                        position.nextStitch.stitch,
                        position.nextStitch.consecutiveCount
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(start = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    }
}

private fun formatStitchDisplay(stitch: xyz.chrismiller.crochetti.domain.model.Stitch, count: Int): String {
    return if (count > 1) {
        "${stitch.abbreviation} $count"
    } else {
        stitch.abbreviation
    }
}
