package xyz.chrismiller.crochetti.domain.model.transfer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.chrismiller.crochetti.domain.model.CustomStitchDefinition
import xyz.chrismiller.crochetti.domain.model.Pattern
import xyz.chrismiller.crochetti.domain.model.PatternComponent
import xyz.chrismiller.crochetti.domain.model.PatternRow
import xyz.chrismiller.crochetti.domain.model.Sided
import xyz.chrismiller.crochetti.domain.model.StitchGroup
import xyz.chrismiller.crochetti.domain.model.StitchInstruction

/**
 * Compact transfer format for patterns, designed for QR code encoding.
 * Uses short field names to minimize payload size.
 *
 * Forward compatibility rules:
 * - New fields MUST have defaults
 * - Fields MUST NOT be removed, only deprecated
 * - Parser MUST use ignoreUnknownKeys = true
 */
@Serializable
data class PatternTransfer(
    /** Version number for format evolution */
    @SerialName("v")
    val version: Int = 1,
    /** Pattern name */
    @SerialName("n")
    val name: String,
    /** Pattern description */
    @SerialName("d")
    val description: String = "",
    /** Pattern components */
    @SerialName("c")
    val components: List<ComponentTransfer>,
    /** Custom stitch definitions */
    @SerialName("cs")
    val customStitches: List<CustomStitchTransfer> = emptyList()
)

/**
 * Compact transfer format for pattern components.
 */
@Serializable
data class ComponentTransfer(
    /** Component name */
    @SerialName("n")
    val name: String,
    /** Component rows */
    @SerialName("r")
    val rows: List<RowTransfer>
)

/**
 * Compact transfer format for pattern rows.
 * Reuses StitchGroup and Sided which already have compact serialization.
 */
@Serializable
data class RowTransfer(
    /** Row instructions - reuses existing StitchGroup serialization */
    @SerialName("i")
    val instructions: List<StitchGroup>,
    /** Row description */
    @SerialName("d")
    val description: String = "",
    /** Sided indicator (RS/WS) */
    @SerialName("s")
    val sided: Sided? = null,
    /** Magic ring indicator */
    @SerialName("m")
    val hasMagicRing: Boolean = false,
    /** Repeat count for row ranges */
    @SerialName("rc")
    val repeatCount: Int = 1
)

/**
 * Compact transfer format for custom stitch definitions.
 * Reuses StitchInstruction which already has compact serialization.
 */
@Serializable
data class CustomStitchTransfer(
    /** Stitch abbreviation */
    @SerialName("a")
    val abbreviation: String,
    /** Display name */
    @SerialName("dn")
    val displayName: String,
    /** Description */
    @SerialName("d")
    val description: String = "",
    /** Primitive instructions */
    @SerialName("ins")
    val instructions: List<StitchInstruction>,
    /** Stitch count produced */
    @SerialName("sc")
    val stitchCount: Int = 1
)

// Extension functions for conversion

/**
 * Convert a Pattern to its transfer format.
 */
fun Pattern.toTransfer(): PatternTransfer = PatternTransfer(
    version = 1,
    name = name,
    description = description,
    components = components.map { it.toTransfer() },
    customStitches = customStitches.map { it.toTransfer() }
)

/**
 * Convert a PatternComponent to its transfer format.
 */
fun PatternComponent.toTransfer(): ComponentTransfer = ComponentTransfer(
    name = name,
    rows = rows.map { it.toTransfer() }
)

/**
 * Convert a PatternRow to its transfer format.
 */
fun PatternRow.toTransfer(): RowTransfer = RowTransfer(
    instructions = instructions,
    description = description,
    sided = sided,
    hasMagicRing = hasMagicRing,
    repeatCount = repeatCount
)

/**
 * Convert a CustomStitchDefinition to its transfer format.
 */
fun CustomStitchDefinition.toTransfer(): CustomStitchTransfer = CustomStitchTransfer(
    abbreviation = abbreviation,
    displayName = displayName,
    description = description,
    instructions = instructions,
    stitchCount = stitchCount
)

/**
 * Convert a PatternTransfer back to a Pattern domain model.
 * IDs will be 0 (to be assigned by database), timestamps will be current time.
 */
fun PatternTransfer.toPattern(): Pattern = Pattern(
    id = 0,
    name = name,
    description = description,
    photoUri = null,
    components = components.mapIndexed { index, ct -> ct.toComponent(index) },
    customStitches = customStitches.map { it.toCustomStitchDefinition() }
)

/**
 * Convert a ComponentTransfer back to a PatternComponent.
 */
fun ComponentTransfer.toComponent(sortOrder: Int = 0): PatternComponent = PatternComponent(
    id = 0,
    name = name,
    rows = rows.map { it.toRow() },
    sortOrder = sortOrder
)

/**
 * Convert a RowTransfer back to a PatternRow.
 */
fun RowTransfer.toRow(): PatternRow = PatternRow(
    id = 0,
    description = description,
    instructions = instructions,
    sided = sided,
    hasMagicRing = hasMagicRing,
    repeatCount = repeatCount
)

/**
 * Convert a CustomStitchTransfer back to a CustomStitchDefinition.
 */
fun CustomStitchTransfer.toCustomStitchDefinition(): CustomStitchDefinition = CustomStitchDefinition(
    abbreviation = abbreviation,
    displayName = displayName,
    description = description,
    instructions = instructions,
    stitchCount = stitchCount,
    rawDsl = "" // Not transferred, can be regenerated if needed
)
