package xyz.chrismiller.crochetti.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.chrismiller.crochetti.domain.model.Pattern
import xyz.chrismiller.crochetti.domain.model.transfer.PatternTransfer
import xyz.chrismiller.crochetti.domain.model.transfer.toPattern

/**
 * Generates a unique name by appending a numeric suffix if needed.
 * "Pattern" -> "Pattern (2)" -> "Pattern (3)", etc.
 */
private fun generateUniqueName(baseName: String, existingNames: Set<String>): String {
    if (baseName !in existingNames) return baseName

    // Strip existing suffix like " (2)" to avoid "Pattern (2) (3)"
    val suffixRegex = """\s*\(\d+\)\s*$""".toRegex()
    val cleanBase = baseName.replace(suffixRegex, "").trim()

    var counter = 2
    while ("$cleanBase ($counter)" in existingNames) counter++
    return "$cleanBase ($counter)"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPreviewBottomSheet(
    patternTransfer: PatternTransfer,
    existingNames: Set<String>,
    onImport: (Pattern) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    // Editable pattern name with auto-suggested unique name
    var editedName by remember {
        mutableStateOf(generateUniqueName(patternTransfer.name, existingNames))
    }

    // Calculate statistics from transfer data
    val totalComponents = patternTransfer.components.size
    val totalRows = patternTransfer.components.sumOf { component ->
        component.rows.sumOf { row -> row.repeatCount }
    }
    val totalStoredRows = patternTransfer.components.sumOf { it.rows.size }
    val customStitchCount = patternTransfer.customStitches.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Import Pattern",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Editable pattern name
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text("Pattern Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description (if any)
            if (patternTransfer.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = patternTransfer.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Pattern statistics
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatRow(
                    label = "Components",
                    value = totalComponents.toString(),
                    detail = patternTransfer.components.joinToString(", ") { it.name }
                )

                StatRow(
                    label = "Total Rows",
                    value = totalRows.toString(),
                    detail = if (totalRows != totalStoredRows) {
                        "$totalStoredRows stored (with repeats)"
                    } else null
                )

                if (customStitchCount > 0) {
                    StatRow(
                        label = "Custom Stitches",
                        value = customStitchCount.toString(),
                        detail = patternTransfer.customStitches.joinToString(", ") { it.abbreviation.uppercase() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val pattern = patternTransfer.copy(name = editedName).toPattern()
                        onImport(pattern)
                    },
                    enabled = editedName.isNotBlank()
                ) {
                    Text("Import Pattern")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    detail: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (detail != null) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
