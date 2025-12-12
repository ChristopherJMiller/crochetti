package xyz.chrismiller.crochetti.domain.model.transfer

import org.junit.Assert.*
import org.junit.Test
import xyz.chrismiller.crochetti.domain.model.CustomStitchDefinition
import xyz.chrismiller.crochetti.domain.model.InsertTarget
import xyz.chrismiller.crochetti.domain.model.Pattern
import xyz.chrismiller.crochetti.domain.model.PatternComponent
import xyz.chrismiller.crochetti.domain.model.PatternRow
import xyz.chrismiller.crochetti.domain.model.PullCount
import xyz.chrismiller.crochetti.domain.model.Sided
import xyz.chrismiller.crochetti.domain.model.Stitch
import xyz.chrismiller.crochetti.domain.model.StitchGroup
import xyz.chrismiller.crochetti.domain.model.StitchInstruction

class PatternTransferTest {

    // ==================== PATTERN TO TRANSFER ====================

    @Test
    fun `minimal pattern converts to transfer`() {
        val pattern = Pattern(
            id = 123,
            name = "Test Pattern",
            components = listOf(
                PatternComponent(
                    id = 1,
                    name = "Body",
                    rows = listOf(
                        PatternRow(
                            id = 1,
                            instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6))
                        )
                    )
                )
            )
        )

        val transfer = pattern.toTransfer()

        assertEquals(1, transfer.version)
        assertEquals("Test Pattern", transfer.name)
        assertEquals("", transfer.description)
        assertEquals(1, transfer.components.size)
        assertEquals("Body", transfer.components[0].name)
        assertEquals(1, transfer.components[0].rows.size)
    }

    @Test
    fun `pattern with description converts correctly`() {
        val pattern = Pattern(
            name = "Amigurumi Bear",
            description = "A cute teddy bear pattern",
            components = listOf(
                PatternComponent(name = "Head", rows = emptyList())
            )
        )

        val transfer = pattern.toTransfer()

        assertEquals("Amigurumi Bear", transfer.name)
        assertEquals("A cute teddy bear pattern", transfer.description)
    }

    @Test
    fun `pattern with multiple components preserves order`() {
        val pattern = Pattern(
            name = "Test",
            components = listOf(
                PatternComponent(name = "Head", rows = emptyList(), sortOrder = 0),
                PatternComponent(name = "Body", rows = emptyList(), sortOrder = 1),
                PatternComponent(name = "Arms", rows = emptyList(), sortOrder = 2)
            )
        )

        val transfer = pattern.toTransfer()

        assertEquals(3, transfer.components.size)
        assertEquals("Head", transfer.components[0].name)
        assertEquals("Body", transfer.components[1].name)
        assertEquals("Arms", transfer.components[2].name)
    }

    @Test
    fun `row with all fields converts correctly`() {
        val row = PatternRow(
            id = 99,
            description = "Work in back loops only",
            instructions = listOf(
                StitchGroup(
                    stitches = listOf(Stitch.SingleCrochet, Stitch.SingleCrochet, Stitch.Increase),
                    repetitions = 6
                )
            ),
            sided = Sided.RightSide,
            hasMagicRing = true,
            repeatCount = 3
        )

        val transfer = row.toTransfer()

        assertEquals("Work in back loops only", transfer.description)
        assertEquals(1, transfer.instructions.size)
        assertEquals(3, transfer.instructions[0].stitches.size)
        assertEquals(6, transfer.instructions[0].repetitions)
        assertEquals(Sided.RightSide, transfer.sided)
        assertTrue(transfer.hasMagicRing)
        assertEquals(3, transfer.repeatCount)
    }

    @Test
    fun `row with defaults has correct default values`() {
        val row = PatternRow(
            instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6))
        )

        val transfer = row.toTransfer()

        assertEquals("", transfer.description)
        assertNull(transfer.sided)
        assertFalse(transfer.hasMagicRing)
        assertEquals(1, transfer.repeatCount)
    }

    @Test
    fun `custom stitch definition converts correctly`() {
        val customStitch = CustomStitchDefinition(
            abbreviation = "pbo",
            displayName = "Partial Bobble",
            description = "A bobble worked partially",
            instructions = listOf(
                StitchInstruction.YarnOver,
                StitchInstruction.Insert(InsertTarget.CurrentStitch),
                StitchInstruction.Pull(PullCount.All)
            ),
            stitchCount = 1,
            rawDsl = "yo insert(st) pull(all)"
        )

        val transfer = customStitch.toTransfer()

        assertEquals("pbo", transfer.abbreviation)
        assertEquals("Partial Bobble", transfer.displayName)
        assertEquals("A bobble worked partially", transfer.description)
        assertEquals(3, transfer.instructions.size)
        assertEquals(1, transfer.stitchCount)
    }

    @Test
    fun `pattern with custom stitches converts correctly`() {
        val pattern = Pattern(
            name = "Custom Pattern",
            components = listOf(
                PatternComponent(name = "Body", rows = emptyList())
            ),
            customStitches = listOf(
                CustomStitchDefinition(
                    abbreviation = "cs1",
                    displayName = "Custom Stitch 1",
                    instructions = listOf(StitchInstruction.YarnOver),
                    stitchCount = 1
                ),
                CustomStitchDefinition(
                    abbreviation = "cs2",
                    displayName = "Custom Stitch 2",
                    instructions = listOf(StitchInstruction.Chain(2)),
                    stitchCount = 2
                )
            )
        )

        val transfer = pattern.toTransfer()

        assertEquals(2, transfer.customStitches.size)
        assertEquals("cs1", transfer.customStitches[0].abbreviation)
        assertEquals("cs2", transfer.customStitches[1].abbreviation)
    }

    // ==================== TRANSFER TO PATTERN ====================

    @Test
    fun `transfer converts back to pattern with new ids`() {
        val transfer = PatternTransfer(
            version = 1,
            name = "Imported Pattern",
            description = "Description",
            components = listOf(
                ComponentTransfer(
                    name = "Body",
                    rows = listOf(
                        RowTransfer(
                            instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6))
                        )
                    )
                )
            )
        )

        val pattern = transfer.toPattern()

        assertEquals(0, pattern.id) // New pattern, no ID yet
        assertEquals("Imported Pattern", pattern.name)
        assertEquals("Description", pattern.description)
        assertNull(pattern.photoUri) // Not transferred
        assertEquals(1, pattern.components.size)
        assertEquals(0, pattern.components[0].id) // New component, no ID yet
    }

    @Test
    fun `transfer preserves component sort order from array position`() {
        val transfer = PatternTransfer(
            name = "Test",
            components = listOf(
                ComponentTransfer(name = "First", rows = emptyList()),
                ComponentTransfer(name = "Second", rows = emptyList()),
                ComponentTransfer(name = "Third", rows = emptyList())
            )
        )

        val pattern = transfer.toPattern()

        assertEquals(0, pattern.components[0].sortOrder)
        assertEquals(1, pattern.components[1].sortOrder)
        assertEquals(2, pattern.components[2].sortOrder)
    }

    @Test
    fun `transfer with custom stitches converts back correctly`() {
        val transfer = PatternTransfer(
            name = "Test",
            components = listOf(ComponentTransfer(name = "Body", rows = emptyList())),
            customStitches = listOf(
                CustomStitchTransfer(
                    abbreviation = "mycs",
                    displayName = "My Custom Stitch",
                    description = "A custom stitch",
                    instructions = listOf(
                        StitchInstruction.YarnOver,
                        StitchInstruction.Insert(),
                        StitchInstruction.Pull()
                    ),
                    stitchCount = 1
                )
            )
        )

        val pattern = transfer.toPattern()

        assertEquals(1, pattern.customStitches.size)
        val cs = pattern.customStitches[0]
        assertEquals("mycs", cs.abbreviation)
        assertEquals("My Custom Stitch", cs.displayName)
        assertEquals("A custom stitch", cs.description)
        assertEquals(3, cs.instructions.size)
        assertEquals(1, cs.stitchCount)
        assertEquals("", cs.rawDsl) // Not transferred
    }

    // ==================== ROUND TRIP TESTS ====================

    @Test
    fun `simple pattern round trip preserves data`() {
        val original = Pattern(
            id = 999,
            name = "Round Trip Test",
            description = "Testing round trip",
            components = listOf(
                PatternComponent(
                    id = 1,
                    name = "Component",
                    rows = listOf(
                        PatternRow(
                            id = 1,
                            instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6))
                        )
                    )
                )
            )
        )

        val transfer = original.toTransfer()
        val restored = transfer.toPattern()

        // IDs should be reset
        assertEquals(0, restored.id)
        assertEquals(0, restored.components[0].id)
        assertEquals(0, restored.components[0].rows[0].id)

        // Data should be preserved
        assertEquals(original.name, restored.name)
        assertEquals(original.description, restored.description)
        assertEquals(original.components.size, restored.components.size)
        assertEquals(original.components[0].name, restored.components[0].name)
        assertEquals(original.components[0].rows.size, restored.components[0].rows.size)

        // Stitch data should match
        val originalStitches = original.components[0].rows[0].instructions[0].stitches
        val restoredStitches = restored.components[0].rows[0].instructions[0].stitches
        assertEquals(originalStitches.size, restoredStitches.size)
        assertEquals(originalStitches, restoredStitches)
    }

    @Test
    fun `complex pattern round trip preserves all data`() {
        val original = Pattern(
            name = "Complex Pattern",
            description = "A complex pattern with all features",
            components = listOf(
                PatternComponent(
                    name = "Head",
                    rows = listOf(
                        PatternRow(
                            description = "Magic ring start",
                            instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6)),
                            hasMagicRing = true,
                            sided = Sided.RightSide
                        ),
                        PatternRow(
                            instructions = listOf(StitchGroup.of(Stitch.Increase, 6)),
                            sided = Sided.WrongSide
                        ),
                        PatternRow(
                            instructions = listOf(
                                StitchGroup(
                                    stitches = listOf(Stitch.SingleCrochet, Stitch.Increase),
                                    repetitions = 6
                                )
                            ),
                            repeatCount = 5
                        )
                    )
                ),
                PatternComponent(
                    name = "Body",
                    rows = listOf(
                        PatternRow(
                            instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 30))
                        )
                    )
                )
            ),
            customStitches = listOf(
                CustomStitchDefinition(
                    abbreviation = "sp",
                    displayName = "Special Stitch",
                    instructions = listOf(
                        StitchInstruction.YarnOver,
                        StitchInstruction.Insert(InsertTarget.CurrentStitch),
                        StitchInstruction.Pull(PullCount.Specific(2))
                    ),
                    stitchCount = 1
                )
            )
        )

        val transfer = original.toTransfer()
        val restored = transfer.toPattern()

        // Components
        assertEquals(2, restored.components.size)
        assertEquals("Head", restored.components[0].name)
        assertEquals("Body", restored.components[1].name)

        // Head rows
        val headRows = restored.components[0].rows
        assertEquals(3, headRows.size)
        assertTrue(headRows[0].hasMagicRing)
        assertEquals(Sided.RightSide, headRows[0].sided)
        assertEquals(Sided.WrongSide, headRows[1].sided)
        assertEquals(5, headRows[2].repeatCount)

        // Custom stitches
        assertEquals(1, restored.customStitches.size)
        assertEquals("sp", restored.customStitches[0].abbreviation)
    }

    @Test
    fun `round trip with all stitch types`() {
        val stitchTypes = listOf(
            Stitch.Chain,
            Stitch.SingleCrochet,
            Stitch.HalfDoubleCrochet,
            Stitch.DoubleCrochet,
            Stitch.Treble,
            Stitch.Increase,
            Stitch.Decrease,
            Stitch.Skip,
            Stitch.FrontPostDc,
            Stitch.BackPostDc,
            Stitch.Custom("custom", 2)
        )

        val original = Pattern(
            name = "All Stitches",
            components = listOf(
                PatternComponent(
                    name = "Test",
                    rows = stitchTypes.mapIndexed { index, stitch ->
                        PatternRow(
                            instructions = listOf(StitchGroup(listOf(stitch), 1))
                        )
                    }
                )
            )
        )

        val transfer = original.toTransfer()
        val restored = transfer.toPattern()

        val restoredRows = restored.components[0].rows
        assertEquals(stitchTypes.size, restoredRows.size)

        stitchTypes.forEachIndexed { index, expectedStitch ->
            val actualStitch = restoredRows[index].instructions[0].stitches[0]
            assertEquals("Stitch at index $index", expectedStitch, actualStitch)
        }
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `empty components list is valid`() {
        val transfer = PatternTransfer(
            name = "Empty",
            components = emptyList()
        )

        val pattern = transfer.toPattern()

        assertEquals("Empty", pattern.name)
        assertTrue(pattern.components.isEmpty())
    }

    @Test
    fun `empty rows list is valid`() {
        val transfer = PatternTransfer(
            name = "Empty Rows",
            components = listOf(
                ComponentTransfer(name = "Empty", rows = emptyList())
            )
        )

        val pattern = transfer.toPattern()

        assertEquals(1, pattern.components.size)
        assertTrue(pattern.components[0].rows.isEmpty())
    }

    @Test
    fun `empty custom stitches list is valid`() {
        val transfer = PatternTransfer(
            name = "No Custom",
            components = listOf(ComponentTransfer(name = "Body", rows = emptyList())),
            customStitches = emptyList()
        )

        val pattern = transfer.toPattern()

        assertTrue(pattern.customStitches.isEmpty())
    }

    @Test
    fun `pattern with only name has correct defaults`() {
        val pattern = Pattern(
            name = "Minimal",
            components = emptyList()
        )

        val transfer = pattern.toTransfer()

        assertEquals(1, transfer.version)
        assertEquals("Minimal", transfer.name)
        assertEquals("", transfer.description)
        assertTrue(transfer.components.isEmpty())
        assertTrue(transfer.customStitches.isEmpty())
    }
}
