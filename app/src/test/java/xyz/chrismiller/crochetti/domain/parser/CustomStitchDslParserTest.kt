package xyz.chrismiller.crochetti.domain.parser

import org.junit.Assert.*
import org.junit.Test
import xyz.chrismiller.crochetti.domain.model.CustomStitchDefinition
import xyz.chrismiller.crochetti.domain.model.InsertTarget
import xyz.chrismiller.crochetti.domain.model.PullCount
import xyz.chrismiller.crochetti.domain.model.StitchInstruction

class CustomStitchDslParserTest {

    // ==================== BASIC PARSING ====================

    @Test
    fun `empty string returns error`() {
        val result = CustomStitchDslParser.parse("", "puff", "Puff Stitch")
        assertTrue(result is CustomStitchDslParser.ParseResult.Error)
    }

    @Test
    fun `blank string returns error`() {
        val result = CustomStitchDslParser.parse("   ", "puff", "Puff Stitch")
        assertTrue(result is CustomStitchDslParser.ParseResult.Error)
    }

    @Test
    fun `simple yo instruction parsed correctly`() {
        val result = CustomStitchDslParser.parse("yo", "test", "Test Stitch")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals(1, def.instructions.size)
        assertEquals(StitchInstruction.YarnOver, def.instructions[0])
    }

    @Test
    fun `multiple yo instructions parsed correctly`() {
        val result = CustomStitchDslParser.parse("yo\nyo\nyo", "test", "Test Stitch")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals(3, def.instructions.size)
        assertTrue(def.instructions.all { it == StitchInstruction.YarnOver })
    }

    // ==================== INSERT INSTRUCTION ====================

    @Test
    fun `insert st parsed correctly`() {
        val result = CustomStitchDslParser.parse("insert(st)", "test", "Test Stitch")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals(1, def.instructions.size)
        val insert = def.instructions[0] as StitchInstruction.Insert
        assertEquals(InsertTarget.CurrentStitch, insert.target)
    }

    @Test
    fun `insert same parsed correctly`() {
        val result = CustomStitchDslParser.parse("insert(same)", "test", "Test Stitch")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        val insert = def.instructions[0] as StitchInstruction.Insert
        assertEquals(InsertTarget.SameStitch, insert.target)
    }

    @Test
    fun `insert with offset parsed correctly`() {
        val result = CustomStitchDslParser.parse("insert(2)", "test", "Test Stitch")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        val insert = def.instructions[0] as StitchInstruction.Insert
        assertTrue(insert.target is InsertTarget.Offset)
        assertEquals(2, (insert.target as InsertTarget.Offset).offset)
    }

    // ==================== PULL INSTRUCTION ====================

    @Test
    fun `simple pull parsed correctly`() {
        val result = CustomStitchDslParser.parse("pull", "test", "Test Stitch")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        val pull = def.instructions[0] as StitchInstruction.Pull
        assertEquals(PullCount.One, pull.count)
    }

    @Test
    fun `pull with specific count parsed correctly`() {
        val result = CustomStitchDslParser.parse("pull(2)", "test", "Test Stitch")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        val pull = def.instructions[0] as StitchInstruction.Pull
        assertTrue(pull.count is PullCount.Specific)
        assertEquals(2, (pull.count as PullCount.Specific).count)
    }

    @Test
    fun `pull all parsed correctly`() {
        val result = CustomStitchDslParser.parse("pull(all)", "test", "Test Stitch")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        val pull = def.instructions[0] as StitchInstruction.Pull
        assertEquals(PullCount.All, pull.count)
    }

    // ==================== CHAIN INSTRUCTION ====================

    @Test
    fun `simple chain parsed correctly`() {
        val result = CustomStitchDslParser.parse("chain", "test", "Test Stitch")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        val chain = def.instructions[0] as StitchInstruction.Chain
        assertEquals(1, chain.count)
    }

    @Test
    fun `chain with count parsed correctly`() {
        val result = CustomStitchDslParser.parse("chain 3", "test", "Test Stitch")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        val chain = def.instructions[0] as StitchInstruction.Chain
        assertEquals(3, chain.count)
    }

    // ==================== REPEAT INSTRUCTION ====================

    @Test
    fun `repeat block parsed correctly`() {
        val result = CustomStitchDslParser.parse(
            """
            repeat 3 {
                yo
                pull
            }
            """.trimIndent(),
            "test", "Test Stitch"
        )
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals(1, def.instructions.size)
        val repeat = def.instructions[0] as StitchInstruction.Repeat
        assertEquals(3, repeat.times)
        assertEquals(2, repeat.instructions.size)
        assertEquals(StitchInstruction.YarnOver, repeat.instructions[0])
        assertTrue(repeat.instructions[1] is StitchInstruction.Pull)
    }

    @Test
    fun `nested repeat blocks parsed correctly`() {
        val result = CustomStitchDslParser.parse(
            """
            repeat 2 {
                yo
                repeat 3 {
                    pull
                }
            }
            """.trimIndent(),
            "test", "Test Stitch"
        )
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        val outer = def.instructions[0] as StitchInstruction.Repeat
        assertEquals(2, outer.times)
        assertEquals(2, outer.instructions.size)
        val inner = outer.instructions[1] as StitchInstruction.Repeat
        assertEquals(3, inner.times)
    }

    // ==================== COMPLETE STITCH DEFINITIONS ====================

    @Test
    fun `basic single crochet definition`() {
        val dsl = """
            insert(st)
            yo
            pull
            yo
            pull(2)
        """.trimIndent()

        val result = CustomStitchDslParser.parse(dsl, "sc", "Single Crochet")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals(5, def.instructions.size)
        assertEquals("sc", def.abbreviation)
        assertEquals("Single Crochet", def.displayName)
    }

    @Test
    fun `puff stitch definition`() {
        val dsl = """
            yo
            insert(st)
            yo
            pull
            repeat 2 {
                yo
                insert(same)
                yo
                pull
            }
            yo
            pull(all)
        """.trimIndent()

        val result = CustomStitchDslParser.parse(dsl, "puff", "Puff Stitch")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals("puff", def.abbreviation)
    }

    @Test
    fun `cluster stitch definition`() {
        val dsl = """
            yo
            insert(st)
            yo
            pull
            yo
            pull(2)
            repeat 2 {
                yo
                insert(same)
                yo
                pull
                yo
                pull(2)
            }
            yo
            pull(all)
        """.trimIndent()

        val result = CustomStitchDslParser.parse(dsl, "cl", "Cluster")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
    }

    // ==================== COMMENTS ====================

    @Test
    fun `line comments are ignored`() {
        val dsl = """
            // This is a comment
            yo
            // Another comment
            pull
        """.trimIndent()

        val result = CustomStitchDslParser.parse(dsl, "test", "Test Stitch")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals(2, def.instructions.size)
    }

    // ==================== STITCH REFERENCES ====================

    @Test
    fun `stitch reference to existing definition`() {
        val existingDefs = mapOf(
            "sc" to CustomStitchDefinition(
                abbreviation = "sc",
                displayName = "Single Crochet",
                instructions = listOf(StitchInstruction.YarnOver),
                stitchCount = 1
            )
        )

        val result = CustomStitchDslParser(existingDefs).parseInstructions(
            "yo\nsc\npull",
            "combo",
            "Combo Stitch"
        )
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals(3, def.instructions.size)
        assertTrue(def.instructions[1] is StitchInstruction.StitchReference)
        assertEquals("sc", (def.instructions[1] as StitchInstruction.StitchReference).stitchAbbreviation)
    }

    @Test
    fun `stitch reference to non-existent definition returns error`() {
        val result = CustomStitchDslParser.parse("yo\nunknownstitch\npull", "test", "Test")
        assertTrue(result is CustomStitchDslParser.ParseResult.Error)
        assertTrue((result as CustomStitchDslParser.ParseResult.Error).message.contains("Unknown stitch"))
    }

    // ==================== STITCH COUNT CALCULATION ====================

    @Test
    fun `simple stitch produces 1 stitch`() {
        val dsl = """
            insert(st)
            yo
            pull
            yo
            pull(all)
        """.trimIndent()

        val result = CustomStitchDslParser.parse(dsl, "sc", "Single Crochet")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals(1, def.stitchCount)
    }

    @Test
    fun `chain produces stitches equal to count`() {
        val result = CustomStitchDslParser.parse("chain 5", "test", "Test")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals(5, def.stitchCount)
    }

    @Test
    fun `stitch count includes referenced stitch counts`() {
        val existingDefs = mapOf(
            "sc" to CustomStitchDefinition(
                abbreviation = "sc",
                displayName = "Single Crochet",
                instructions = listOf(StitchInstruction.YarnOver),
                stitchCount = 1
            )
        )

        val result = CustomStitchDslParser(existingDefs).parseInstructions(
            "sc\nsc\nsc",
            "triple",
            "Triple SC"
        )
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals(3, def.stitchCount)
    }

    // ==================== ERROR HANDLING ====================

    @Test
    fun `unexpected character returns error`() {
        val result = CustomStitchDslParser.parse("yo @@ pull", "test", "Test")
        assertTrue(result is CustomStitchDslParser.ParseResult.Error)
    }

    @Test
    fun `unclosed repeat brace returns error`() {
        val result = CustomStitchDslParser.parse(
            """
            repeat 3 {
                yo
            """.trimIndent(),
            "test", "Test"
        )
        assertTrue(result is CustomStitchDslParser.ParseResult.Error)
    }

    @Test
    fun `missing repeat count returns error`() {
        val result = CustomStitchDslParser.parse("repeat { yo }", "test", "Test")
        assertTrue(result is CustomStitchDslParser.ParseResult.Error)
    }

    @Test
    fun `missing insert target returns error`() {
        val result = CustomStitchDslParser.parse("insert()", "test", "Test")
        assertTrue(result is CustomStitchDslParser.ParseResult.Error)
    }

    @Test
    fun `error includes line number`() {
        val result = CustomStitchDslParser.parse(
            """
            yo
            yo
            badinstruction
            yo
            """.trimIndent(),
            "test", "Test"
        )
        assertTrue(result is CustomStitchDslParser.ParseResult.Error)
        val error = result as CustomStitchDslParser.ParseResult.Error
        assertEquals(3, error.line)
    }

    // ==================== CASE INSENSITIVITY ====================

    @Test
    fun `keywords are case insensitive`() {
        val dsl = """
            YO
            Insert(ST)
            PULL(ALL)
        """.trimIndent()

        val result = CustomStitchDslParser.parse(dsl, "test", "Test")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals(3, def.instructions.size)
    }

    // ==================== WHITESPACE HANDLING ====================

    @Test
    fun `whitespace between instructions is ignored`() {
        val dsl = """
            yo


            pull

            chain
        """.trimIndent()

        val result = CustomStitchDslParser.parse(dsl, "test", "Test")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals(3, def.instructions.size)
    }

    @Test
    fun `inline whitespace is handled correctly`() {
        val result = CustomStitchDslParser.parse("yo   pull    chain", "test", "Test")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals(3, def.instructions.size)
    }

    // ==================== DEFINITION METADATA ====================

    @Test
    fun `abbreviation is normalized to lowercase`() {
        val result = CustomStitchDslParser.parse("yo", "PUFF", "Puff Stitch")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals("puff", def.normalizedAbbreviation)
    }

    @Test
    fun `description is preserved`() {
        val result = CustomStitchDslParser.parse("yo", "test", "Test", "A test stitch for testing")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals("A test stitch for testing", def.description)
    }

    @Test
    fun `raw DSL is preserved`() {
        val dsl = "yo\npull\nchain"
        val result = CustomStitchDslParser.parse(dsl, "test", "Test")
        assertTrue(result is CustomStitchDslParser.ParseResult.Success)
        val def = (result as CustomStitchDslParser.ParseResult.Success).definition
        assertEquals(dsl, def.rawDsl)
    }
}
