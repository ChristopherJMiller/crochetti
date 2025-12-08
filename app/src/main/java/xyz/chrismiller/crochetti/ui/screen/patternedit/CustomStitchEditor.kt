package xyz.chrismiller.crochetti.ui.screen.patternedit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import xyz.chrismiller.crochetti.domain.model.CustomStitchDefinition
import xyz.chrismiller.crochetti.domain.model.InstructionState
import xyz.chrismiller.crochetti.domain.parser.CustomStitchDslParser

/**
 * Collapsible section showing custom stitches defined for this pattern.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomStitchesSection(
    customStitches: List<CustomStitchDefinition>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onAddCustomStitch: () -> Unit,
    onEditCustomStitch: (CustomStitchDefinition) -> Unit,
    onDeleteCustomStitch: (CustomStitchDefinition) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header row - always visible
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Custom Stitches",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (customStitches.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${customStitches.size})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onAddCustomStitch) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                                      else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    if (customStitches.isEmpty()) {
                        Text(
                            text = "No custom stitches defined. Add one to use in your pattern rows.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            customStitches.forEach { stitch ->
                                CustomStitchChip(
                                    definition = stitch,
                                    onEdit = { onEditCustomStitch(stitch) },
                                    onDelete = { onDeleteCustomStitch(stitch) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomStitchChip(
    definition: CustomStitchDefinition,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onEdit,
        label = {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = definition.abbreviation.uppercase(),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "${definition.displayName} (${definition.stitchCount} st)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                modifier = Modifier.size(16.dp)
            )
        },
        trailingIcon = {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Modal bottom sheet for editing/creating a custom stitch definition.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomStitchEditorSheet(
    editState: CustomStitchEditState,
    onAbbreviationChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDslChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editState.isEditing) "Edit Custom Stitch" else "New Custom Stitch",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Abbreviation field
            OutlinedTextField(
                value = editState.abbreviation,
                onValueChange = onAbbreviationChange,
                label = { Text("Abbreviation") },
                placeholder = { Text("e.g., puff, cl, popcorn") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = {
                    Text("Used in row patterns (e.g., 'puff 6' or 'cl, sc 3')")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Display name field
            OutlinedTextField(
                value = editState.displayName,
                onValueChange = onDisplayNameChange,
                label = { Text("Display Name") },
                placeholder = { Text("e.g., Puff Stitch, Cluster") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description field
            OutlinedTextField(
                value = editState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description (optional)") },
                placeholder = { Text("Brief description of the stitch") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // DSL Instructions
            Text(
                text = "Stitch Instructions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // DSL help text
            Text(
                text = "Define how the stitch is worked using these commands:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    DslHelpItem("yo", "Yarn over")
                    DslHelpItem("insert(st)", "Insert hook into stitch")
                    DslHelpItem("insert(same)", "Insert into same stitch")
                    DslHelpItem("pull", "Pull up a loop")
                    DslHelpItem("pull(2)", "Pull through 2 loops")
                    DslHelpItem("pull(all)", "Pull through all loops")
                    DslHelpItem("chain", "Make a chain stitch")
                    DslHelpItem("repeat N { }", "Repeat N times")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // DSL input
            OutlinedTextField(
                value = editState.dslText,
                onValueChange = onDslChange,
                label = { Text("Instructions DSL") },
                placeholder = {
                    Text(
                        text = "yo\ninsert(st)\nyo\npull\nyo\npull(2)\nyo\npull(all)",
                        fontFamily = FontFamily.Monospace
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 6,
                maxLines = 12,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                isError = editState.parseResult is CustomStitchDslParser.ParseResult.Error,
                supportingText = {
                    when (val result = editState.parseResult) {
                        is CustomStitchDslParser.ParseResult.Error -> {
                            Text(
                                text = "Line ${result.line}: ${result.message}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        is CustomStitchDslParser.ParseResult.Success -> {
                            Text(
                                text = "Valid! Produces ${result.definition.stitchCount} stitch(es)",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        null -> {
                            if (editState.abbreviation.isBlank() ||
                                editState.displayName.isBlank() ||
                                editState.dslText.isBlank()) {
                                Text("Fill in all fields to validate")
                            }
                        }
                    }
                }
            )

            // Loop state preview - only show when parse is successful
            val parseResult = editState.parseResult
            if (parseResult is CustomStitchDslParser.ParseResult.Success &&
                parseResult.loopStates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LoopStatePreview(loopStates = parseResult.loopStates)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = editState.parseResult is CustomStitchDslParser.ParseResult.Success
                ) {
                    Text(if (editState.isEditing) "Save Changes" else "Add Stitch")
                }
            }
        }
    }
}

@Composable
private fun DslHelpItem(
    command: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = command,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = "- $description",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Shows the loop count after each instruction for visualization.
 */
@Composable
fun LoopStatePreview(
    loopStates: List<InstructionState>,
    modifier: Modifier = Modifier
) {
    if (loopStates.isEmpty()) return

    val finalLoops = loopStates.lastOrNull()?.loopsAfter ?: 1
    val isValid = finalLoops == 1

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Loop Preview",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!isValid) {
                    Text(
                        text = "Ends with $finalLoops loops",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            loopStates.forEach { state ->
                val indent = "  ".repeat(state.depth)
                val isLast = state == loopStates.last()
                val loopText = if (state.loopsAfter == 1) "1 loop" else "${state.loopsAfter} loops"
                val suffix = if (isLast && isValid) " ✓" else ""

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$indent${state.instruction.toDisplayString()}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "[$loopText]$suffix",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (isLast && isValid) {
                            MaterialTheme.colorScheme.primary
                        } else if (isLast && !isValid) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
