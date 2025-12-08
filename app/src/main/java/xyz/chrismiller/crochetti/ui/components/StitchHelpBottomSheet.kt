package xyz.chrismiller.crochetti.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import xyz.chrismiller.crochetti.domain.model.Stitch
import xyz.chrismiller.crochetti.domain.model.StitchCategory
import xyz.chrismiller.crochetti.domain.model.StitchDifficulty
import xyz.chrismiller.crochetti.domain.model.StitchGlossary
import xyz.chrismiller.crochetti.domain.model.StitchInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StitchHelpBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    currentRowStitches: List<Stitch> = emptyList(),
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val expandedCategories = remember { mutableStateMapOf<StitchCategory, Boolean>() }
    val expandedStitches = remember { mutableStateMapOf<Stitch, Boolean>() }

    // Get contextual stitches from current row
    val contextualStitches = remember(currentRowStitches) {
        StitchGlossary.getInfoForStitches(currentRowStitches)
    }

    // Get all stitches, filtered by search
    val allStitchesByCategory = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            StitchGlossary.groupedByCategory()
        } else {
            StitchGlossary.search(searchQuery).groupBy { it.category }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Title
            Text(
                text = "Stitch Guide",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search stitches...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Contextual help - stitches in current row
                if (contextualStitches.isNotEmpty() && searchQuery.isBlank()) {
                    item {
                        ContextualHelpSection(
                            stitches = contextualStitches,
                            expandedStitches = expandedStitches,
                            onToggleExpand = { stitch ->
                                expandedStitches[stitch] = !(expandedStitches[stitch] ?: false)
                            }
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                // All stitches by category
                StitchCategory.entries.forEach { category ->
                    val stitchesInCategory = allStitchesByCategory[category] ?: emptyList()
                    if (stitchesInCategory.isNotEmpty()) {
                        item {
                            CategorySection(
                                category = category,
                                stitches = stitchesInCategory,
                                isExpanded = expandedCategories[category] ?: (searchQuery.isNotBlank()),
                                expandedStitches = expandedStitches,
                                onToggleCategory = {
                                    expandedCategories[category] = !(expandedCategories[category] ?: false)
                                },
                                onToggleStitch = { stitch ->
                                    expandedStitches[stitch] = !(expandedStitches[stitch] ?: false)
                                }
                            )
                        }
                    }
                }

                // Bottom padding
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ContextualHelpSection(
    stitches: List<StitchInfo>,
    expandedStitches: Map<Stitch, Boolean>,
    onToggleExpand: (Stitch) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Stitches in Current Row",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        stitches.forEach { info ->
            StitchCard(
                info = info,
                isExpanded = expandedStitches[info.stitch] ?: false,
                onToggle = { onToggleExpand(info.stitch) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CategorySection(
    category: StitchCategory,
    stitches: List<StitchInfo>,
    isExpanded: Boolean,
    expandedStitches: Map<Stitch, Boolean>,
    onToggleCategory: () -> Unit,
    onToggleStitch: (Stitch) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Category header
        Surface(
            onClick = onToggleCategory,
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${stitches.size} stitches",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
        }

        // Category content
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                stitches.forEach { info ->
                    StitchCard(
                        info = info,
                        isExpanded = expandedStitches[info.stitch] ?: false,
                        onToggle = { onToggleStitch(info.stitch) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StitchCard(
    info: StitchInfo,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Abbreviation chip
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                text = info.stitch.abbreviation.uppercase(),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.size(width = 72.dp, height = 32.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Name and difficulty
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = info.stitch.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        DifficultyLabel(difficulty = info.difficulty)
                    }
                }

                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    // Description
                    Text(
                        text = info.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Instructions
                    Text(
                        text = "How to:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = info.instructions,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // UK equivalent if different
                    info.ukEquivalent?.let { uk ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "UK term: $uk",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Stitch count info
                    if (info.stitch.stitchCount != 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (info.stitch.stitchCount) {
                                0 -> "Does not add stitches"
                                2 -> "Produces 2 stitches"
                                else -> "Produces ${info.stitch.stitchCount} stitches"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DifficultyLabel(
    difficulty: StitchDifficulty,
    modifier: Modifier = Modifier
) {
    val color = when (difficulty) {
        StitchDifficulty.BEGINNER -> MaterialTheme.colorScheme.tertiary
        StitchDifficulty.INTERMEDIATE -> MaterialTheme.colorScheme.secondary
        StitchDifficulty.ADVANCED -> MaterialTheme.colorScheme.error
    }

    Text(
        text = difficulty.displayName,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
    )
}
