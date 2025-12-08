package xyz.chrismiller.crochetti.ui.screen.patternedit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import xyz.chrismiller.crochetti.domain.model.Sided
import xyz.chrismiller.crochetti.domain.parser.PatternDslParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternEditScreen(
    patternId: Long?,
    onNavigateBack: () -> Unit,
    onPatternSaved: (Long) -> Unit,
    viewModel: PatternEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load pattern if editing
    LaunchedEffect(patternId) {
        if (patternId != null) {
            viewModel.loadPattern(patternId)
        }
    }

    // Handle save success
    LaunchedEffect(uiState.savedPatternId) {
        uiState.savedPatternId?.let { savedId ->
            onPatternSaved(savedId)
        }
    }

    // Show error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (patternId == null) "New Pattern" else "Edit Pattern")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Small FAB for adding rows (animated based on scroll)
                AnimatedVisibility(
                    visible = uiState.showAddRowFab,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it }
                ) {
                    SmallFloatingActionButton(
                        onClick = { viewModel.addRow(uiState.selectedComponentIndex) },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Row"
                        )
                    }
                }

                // Main save FAB
                FloatingActionButton(
                    onClick = { viewModel.savePattern(patternId) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save"
                        )
                    }
                }
            }
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
            PatternEditContent(
                uiState = uiState,
                onNameChange = viewModel::updateName,
                onDescriptionChange = viewModel::updateDescription,
                onSelectComponent = viewModel::selectComponent,
                onComponentNameChange = viewModel::updateComponentName,
                onAddComponent = viewModel::addComponent,
                onRemoveComponent = viewModel::removeComponent,
                onRowDslChange = viewModel::updateRowDsl,
                onRowDescriptionChange = viewModel::updateRowDescription,
                onRowSidedChange = viewModel::updateRowSided,
                onRowMagicRingChange = viewModel::updateRowMagicRing,
                onAddRow = viewModel::addRow,
                onRemoveRow = viewModel::removeRow,
                onScrollStateChange = viewModel::updateShowAddRowFab,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

// Key for the "Add Row" header item to track its visibility
private const val ADD_ROW_HEADER_KEY = "add_row_header"

@Composable
private fun PatternEditContent(
    uiState: PatternEditUiState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSelectComponent: (Int) -> Unit,
    onComponentNameChange: (Int, String) -> Unit,
    onAddComponent: () -> Unit,
    onRemoveComponent: (Int) -> Unit,
    onRowDslChange: (Int, Int, String) -> Unit,
    onRowDescriptionChange: (Int, Int, String) -> Unit,
    onRowSidedChange: (Int, Int, Sided?) -> Unit,
    onRowMagicRingChange: (Int, Int, Boolean) -> Unit,
    onAddRow: (Int) -> Unit,
    onRemoveRow: (Int, Int) -> Unit,
    onScrollStateChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Track if the "Add Row" header is visible - if not, show floating FAB
    val showFab by remember {
        derivedStateOf {
            // Show FAB when we've scrolled past the basic info section (roughly 5+ items)
            listState.firstVisibleItemIndex >= 5
        }
    }

    // Report scroll state changes to parent
    LaunchedEffect(showFab) {
        onScrollStateChange(showFab)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Basic Info Section
        item {
            Text(
                text = "Basic Info",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text("Pattern Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
        }

        // Components Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Components",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onAddComponent) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }
        }

        // Component Tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(uiState.components) { index, component ->
                    FilterChip(
                        selected = index == uiState.selectedComponentIndex,
                        onClick = { onSelectComponent(index) },
                        label = { Text(component.name) },
                        trailingIcon = if (uiState.components.size > 1) {
                            {
                                IconButton(
                                    onClick = { onRemoveComponent(index) },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        } else null
                    )
                }
            }
        }

        // Selected Component Edit
        val selectedComponent = uiState.components.getOrNull(uiState.selectedComponentIndex)
        if (selectedComponent != null) {
            item {
                OutlinedTextField(
                    value = selectedComponent.name,
                    onValueChange = { onComponentNameChange(uiState.selectedComponentIndex, it) },
                    label = { Text("Component Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Rows
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rows",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { onAddRow(uiState.selectedComponentIndex) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Row")
                    }
                }
            }

            itemsIndexed(selectedComponent.rows) { rowIndex, row ->
                RowEditCard(
                    rowNumber = rowIndex + 1,
                    rowState = row,
                    canDelete = selectedComponent.rows.size > 1,
                    isFirstRow = rowIndex == 0,
                    onDslChange = { onRowDslChange(uiState.selectedComponentIndex, rowIndex, it) },
                    onDescriptionChange = { onRowDescriptionChange(uiState.selectedComponentIndex, rowIndex, it) },
                    onSidedChange = { onRowSidedChange(uiState.selectedComponentIndex, rowIndex, it) },
                    onMagicRingChange = { onRowMagicRingChange(uiState.selectedComponentIndex, rowIndex, it) },
                    onDelete = { onRemoveRow(uiState.selectedComponentIndex, rowIndex) }
                )
            }
        }

        // Bottom spacer for FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun RowEditCard(
    rowNumber: Int,
    rowState: RowEditState,
    canDelete: Boolean,
    isFirstRow: Boolean,
    onDslChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSidedChange: (Sided?) -> Unit,
    onMagicRingChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Row $rowNumber",
                    style = MaterialTheme.typography.titleSmall
                )
                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete row",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // DSL Input
            OutlinedTextField(
                value = rowState.dslText,
                onValueChange = onDslChange,
                label = { Text("Pattern (e.g., sc 6, (sc 2, inc) x6)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = rowState.parseResult is PatternDslParser.ParseResult.Error,
                supportingText = {
                    when (val result = rowState.parseResult) {
                        is PatternDslParser.ParseResult.Error -> {
                            Text(
                                text = result.message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        is PatternDslParser.ParseResult.Success -> {
                            if (result.groups.isNotEmpty()) {
                                val stitchCount = result.groups.sumOf { it.totalStitchCount }
                                Text(
                                    text = "$stitchCount stitches",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        null -> {}
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sided and Magic Ring chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = rowState.sided == Sided.RightSide,
                    onClick = {
                        onSidedChange(if (rowState.sided == Sided.RightSide) null else Sided.RightSide)
                    },
                    label = { Text("RS") }
                )
                FilterChip(
                    selected = rowState.sided == Sided.WrongSide,
                    onClick = {
                        onSidedChange(if (rowState.sided == Sided.WrongSide) null else Sided.WrongSide)
                    },
                    label = { Text("WS") }
                )
                // Magic Ring option only for Row 1
                if (isFirstRow) {
                    FilterChip(
                        selected = rowState.hasMagicRing,
                        onClick = { onMagicRingChange(!rowState.hasMagicRing) },
                        label = { Text("MR") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            OutlinedTextField(
                value = rowState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
