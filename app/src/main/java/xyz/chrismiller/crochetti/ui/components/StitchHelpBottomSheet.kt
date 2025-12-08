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
import xyz.chrismiller.crochetti.domain.model.CustomStitchDefinition
import xyz.chrismiller.crochetti.domain.model.Sided
import xyz.chrismiller.crochetti.domain.model.Stitch
import xyz.chrismiller.crochetti.domain.model.StitchCategory
import xyz.chrismiller.crochetti.domain.model.StitchDifficulty
import xyz.chrismiller.crochetti.domain.model.StitchGlossary
import xyz.chrismiller.crochetti.domain.model.StitchInfo
import xyz.chrismiller.crochetti.domain.model.TechniqueGlossary
import xyz.chrismiller.crochetti.domain.model.TechniqueInfo
import xyz.chrismiller.crochetti.domain.model.toReadableSteps

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StitchHelpBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    currentRowStitches: List<Stitch> = emptyList(),
    customStitches: List<CustomStitchDefinition> = emptyList(),
    hasMagicRing: Boolean = false,
    sided: Sided? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val expandedCategories = remember { mutableStateMapOf<StitchCategory, Boolean>() }
    val expandedStitches = remember { mutableStateMapOf<Stitch, Boolean>() }
    val expandedCustomStitches = remember { mutableStateMapOf<String, Boolean>() }
    val expandedTechniques = remember { mutableStateMapOf<String, Boolean>() }
    var customStitchSectionExpanded by remember { mutableStateOf(false) }

    // Get contextual stitches from current row (standard stitches only)
    val contextualStitches = remember(currentRowStitches) {
        StitchGlossary.getInfoForStitches(currentRowStitches)
    }

    // Get custom stitches used in current row
    val contextualCustomStitches = remember(currentRowStitches, customStitches) {
        val customStitchNames = currentRowStitches
            .filterIsInstance<Stitch.Custom>()
            .map { it.name.lowercase() }
            .toSet()
        customStitches.filter { it.normalizedAbbreviation in customStitchNames }
    }

    // Get techniques relevant to current row
    val contextualTechniques = remember(hasMagicRing, sided) {
        buildList {
            if (hasMagicRing) add(TechniqueGlossary.magicRing)
            sided?.let { add(TechniqueGlossary.getForSided(it)) }
        }
    }

    // Filter custom stitches by search query
    val filteredCustomStitches = remember(searchQuery, customStitches) {
        if (searchQuery.isBlank()) {
            customStitches
        } else {
            val query = searchQuery.lowercase()
            customStitches.filter { cs ->
                cs.abbreviation.lowercase().contains(query) ||
                cs.displayName.lowercase().contains(query) ||
                cs.description.lowercase().contains(query)
            }
        }
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
                // Contextual help - stitches and techniques in current row
                val hasContextualContent = (contextualStitches.isNotEmpty() || contextualCustomStitches.isNotEmpty() || contextualTechniques.isNotEmpty()) && searchQuery.isBlank()
                if (hasContextualContent) {
                    item {
                        ContextualHelpSection(
                            stitches = contextualStitches,
                            customStitches = contextualCustomStitches,
                            techniques = contextualTechniques,
                            expandedStitches = expandedStitches,
                            expandedCustomStitches = expandedCustomStitches,
                            expandedTechniques = expandedTechniques,
                            onToggleExpand = { stitch ->
                                expandedStitches[stitch] = !(expandedStitches[stitch] ?: false)
                            },
                            onToggleCustomExpand = { abbrev ->
                                expandedCustomStitches[abbrev] = !(expandedCustomStitches[abbrev] ?: false)
                            },
                            onToggleTechniqueExpand = { abbrev ->
                                expandedTechniques[abbrev] = !(expandedTechniques[abbrev] ?: false)
                            }
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                // Custom Stitches section (if pattern has any)
                if (filteredCustomStitches.isNotEmpty()) {
                    item {
                        CustomStitchesSection(
                            customStitches = filteredCustomStitches,
                            isExpanded = customStitchSectionExpanded || searchQuery.isNotBlank(),
                            expandedCustomStitches = expandedCustomStitches,
                            onToggleSection = { customStitchSectionExpanded = !customStitchSectionExpanded },
                            onToggleStitch = { abbrev ->
                                expandedCustomStitches[abbrev] = !(expandedCustomStitches[abbrev] ?: false)
                            }
                        )
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
    customStitches: List<CustomStitchDefinition>,
    techniques: List<TechniqueInfo>,
    expandedStitches: Map<Stitch, Boolean>,
    expandedCustomStitches: Map<String, Boolean>,
    expandedTechniques: Map<String, Boolean>,
    onToggleExpand: (Stitch) -> Unit,
    onToggleCustomExpand: (String) -> Unit,
    onToggleTechniqueExpand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Current Row Help",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Techniques first (MR, RS/WS)
        techniques.forEach { technique ->
            TechniqueCard(
                technique = technique,
                isExpanded = expandedTechniques[technique.abbreviation] ?: false,
                onToggle = { onToggleTechniqueExpand(technique.abbreviation) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Standard stitches
        stitches.forEach { info ->
            StitchCard(
                info = info,
                isExpanded = expandedStitches[info.stitch] ?: false,
                onToggle = { onToggleExpand(info.stitch) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Custom stitches in current row
        customStitches.forEach { customStitch ->
            CustomStitchCard(
                customStitch = customStitch,
                isExpanded = expandedCustomStitches[customStitch.abbreviation] ?: false,
                onToggle = { onToggleCustomExpand(customStitch.abbreviation) }
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
                        }
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

@Composable
private fun CustomStitchesSection(
    customStitches: List<CustomStitchDefinition>,
    isExpanded: Boolean,
    expandedCustomStitches: Map<String, Boolean>,
    onToggleSection: () -> Unit,
    onToggleStitch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section header
        Surface(
            onClick = onToggleSection,
            color = MaterialTheme.colorScheme.tertiaryContainer,
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
                        text = "Custom Stitches",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "${customStitches.size} stitch${if (customStitches.size != 1) "es" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        // Section content
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                customStitches.forEach { customStitch ->
                    CustomStitchCard(
                        customStitch = customStitch,
                        isExpanded = expandedCustomStitches[customStitch.abbreviation] ?: false,
                        onToggle = { onToggleStitch(customStitch.abbreviation) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomStitchCard(
    customStitch: CustomStitchDefinition,
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
                                text = customStitch.abbreviation.uppercase(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Name and "Custom" label
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = customStitch.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Custom",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
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
                    // Description (if provided)
                    if (customStitch.description.isNotBlank()) {
                        Text(
                            text = customStitch.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Instructions
                    Text(
                        text = "How to:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = customStitch.instructions.toReadableSteps(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Stitch count info
                    if (customStitch.stitchCount != 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (customStitch.stitchCount) {
                                0 -> "Does not add stitches"
                                2 -> "Produces 2 stitches"
                                else -> "Produces ${customStitch.stitchCount} stitches"
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
private fun TechniqueCard(
    technique: TechniqueInfo,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
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
                                text = technique.abbreviation,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Name and "Technique" label
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = technique.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Technique",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
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
                        text = technique.description,
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
                        text = technique.instructions,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Tips (if available)
                    technique.tips?.let { tips ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tips:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = tips,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
