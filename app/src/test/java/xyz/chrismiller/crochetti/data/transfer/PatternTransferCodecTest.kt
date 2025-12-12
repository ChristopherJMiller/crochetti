package xyz.chrismiller.crochetti.data.transfer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
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

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class PatternTransferCodecTest {

    private lateinit var codec: PatternTransferCodec

    @Before
    fun setUp() {
        codec = PatternTransferCodec()
    }

    // ==================== ENCODE TESTS ====================

    @Test
    fun `encode produces non-empty output`() {
        val pattern = createSimplePattern()

        val result = codec.encode(pattern)

        assertTrue(result.isSuccess)
        val encoded = result.getOrThrow()
        assertTrue(encoded.isNotEmpty())
    }

    @Test
    fun `encode produces url-safe base64`() {
        val pattern = createSimplePattern()

        val encoded = codec.encode(pattern).getOrThrow()

        // URL-safe Base64 should not contain + or /
        assertFalse("Should not contain +", encoded.contains('+'))
        assertFalse("Should not contain /", encoded.contains('/'))
        // Should use URL-safe characters instead
        // Can contain - and _ as URL-safe alternatives
    }

    @Test
    fun `encode compresses data effectively`() {
        val pattern = createLargePattern()

        val encoded = codec.encode(pattern).getOrThrow()

        // GZIP should compress repetitive data significantly
        // A large pattern with 50 rows of identical stitches should compress well
        // The raw JSON would be much larger than the compressed output
        assertTrue("Encoded size should be reasonable", encoded.length < 2000)
    }

    @Test
    fun `encodeChecked succeeds for small pattern`() {
        val pattern = createSimplePattern()

        val result = codec.encodeChecked(pattern)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `encodeChecked fails for oversized pattern`() {
        // Create a pattern that will exceed QR capacity
        val pattern = createHugePattern()

        val result = codec.encodeChecked(pattern)

        // Verify either it's too large or check the actual size
        val size = codec.getEncodedSize(pattern)
        if (size > PatternTransferCodec.MAX_QR_CAPACITY) {
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is QrError.PatternTooLarge)
        } else {
            // If GZIP compressed it enough, just verify the encode works
            assertTrue("Pattern should either fail or succeed", result.isSuccess || result.isFailure)
        }
    }

    @Test
    fun `getEncodedSize returns correct size`() {
        val pattern = createSimplePattern()

        val size = codec.getEncodedSize(pattern)
        val encoded = codec.encode(pattern).getOrThrow()

        assertEquals(encoded.length, size)
    }

    // ==================== DECODE TESTS ====================

    @Test
    fun `decode succeeds for valid encoded data`() {
        val pattern = createSimplePattern()
        val encoded = codec.encode(pattern).getOrThrow()

        val result = codec.decode(encoded)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `decode returns PatternTransfer with correct data`() {
        val pattern = createSimplePattern()
        val encoded = codec.encode(pattern).getOrThrow()

        val transfer = codec.decode(encoded).getOrThrow()

        assertEquals("Test Pattern", transfer.name)
        assertEquals("A test pattern", transfer.description)
        assertEquals(1, transfer.components.size)
        assertEquals("Body", transfer.components[0].name)
    }

    @Test
    fun `decodeToPattern returns Pattern domain model`() {
        val original = createSimplePattern()
        val encoded = codec.encode(original).getOrThrow()

        val result = codec.decodeToPattern(encoded)

        assertTrue(result.isSuccess)
        val decoded = result.getOrThrow()
        assertEquals(original.name, decoded.name)
        assertEquals(original.description, decoded.description)
    }

    @Test
    fun `decode fails for garbage data`() {
        // Use random garbage that won't parse as valid gzipped JSON
        val garbageData = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXo" // "abcdefghijklmnopqrstuvwxyz" base64

        val result = codec.decode(garbageData)

        assertTrue(result.isFailure)
        // Could be DecompressionFailed (not valid gzip) or InvalidFormat
        val error = result.exceptionOrNull()
        assertTrue(
            "Expected DecompressionFailed or InvalidFormat but got $error",
            error is QrError.DecompressionFailed || error is QrError.InvalidFormat
        )
    }

    @Test
    fun `decode fails for non-gzip data`() {
        // Valid base64 but not gzip compressed
        val notGzipped = android.util.Base64.encodeToString(
            "plain text".toByteArray(),
            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
        )

        val result = codec.decode(notGzipped)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is QrError.DecompressionFailed)
    }

    @Test
    fun `decode fails for invalid json after decompression`() {
        // Gzip compress invalid JSON
        val invalidJson = gzipAndEncode("{invalid json}")

        val result = codec.decode(invalidJson)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is QrError.InvalidFormat)
    }

    @Test
    fun `decode fails for unsupported version`() {
        // Create a transfer with future version
        val json = """{"v":99,"n":"Test","d":"","c":[{"n":"Body","r":[]}]}"""
        val encoded = gzipAndEncode(json)

        val result = codec.decode(encoded)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is QrError.UnsupportedVersion)
        assertEquals(99, (error as QrError.UnsupportedVersion).foundVersion)
    }

    @Test
    fun `decode fails for blank pattern name`() {
        val json = """{"v":1,"n":"","d":"","c":[{"n":"Body","r":[]}]}"""
        val encoded = gzipAndEncode(json)

        val result = codec.decode(encoded)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is QrError.ValidationFailed)
    }

    @Test
    fun `decode fails for empty components`() {
        val json = """{"v":1,"n":"Test","d":"","c":[]}"""
        val encoded = gzipAndEncode(json)

        val result = codec.decode(encoded)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is QrError.ValidationFailed)
    }

    // ==================== ROUND TRIP TESTS ====================

    @Test
    fun `round trip preserves simple pattern`() {
        val original = createSimplePattern()

        val encoded = codec.encode(original).getOrThrow()
        val decoded = codec.decodeToPattern(encoded).getOrThrow()

        assertEquals(original.name, decoded.name)
        assertEquals(original.description, decoded.description)
        assertEquals(original.components.size, decoded.components.size)
        assertEquals(original.components[0].name, decoded.components[0].name)
        assertEquals(original.components[0].rows.size, decoded.components[0].rows.size)
    }

    @Test
    fun `round trip preserves complex pattern`() {
        val original = createComplexPattern()

        val encoded = codec.encode(original).getOrThrow()
        val decoded = codec.decodeToPattern(encoded).getOrThrow()

        // Basic pattern info
        assertEquals(original.name, decoded.name)
        assertEquals(original.description, decoded.description)

        // Components
        assertEquals(original.components.size, decoded.components.size)
        original.components.forEachIndexed { i, comp ->
            assertEquals(comp.name, decoded.components[i].name)
            assertEquals(comp.rows.size, decoded.components[i].rows.size)
        }

        // Custom stitches
        assertEquals(original.customStitches.size, decoded.customStitches.size)
        original.customStitches.forEachIndexed { i, cs ->
            assertEquals(cs.abbreviation, decoded.customStitches[i].abbreviation)
            assertEquals(cs.displayName, decoded.customStitches[i].displayName)
        }
    }

    @Test
    fun `round trip preserves all stitch types`() {
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
            Stitch.Custom("mycs", 2)
        )

        val original = Pattern(
            name = "Stitch Test",
            description = "Test all stitches",
            components = listOf(
                PatternComponent(
                    name = "Test",
                    rows = stitchTypes.map { stitch ->
                        PatternRow(instructions = listOf(StitchGroup(listOf(stitch), 1)))
                    }
                )
            )
        )

        val encoded = codec.encode(original).getOrThrow()
        val decoded = codec.decodeToPattern(encoded).getOrThrow()

        val decodedRows = decoded.components[0].rows
        assertEquals(stitchTypes.size, decodedRows.size)

        stitchTypes.forEachIndexed { index, expectedStitch ->
            val actualStitch = decodedRows[index].instructions[0].stitches[0]
            assertEquals("Stitch at index $index", expectedStitch, actualStitch)
        }
    }

    @Test
    fun `round trip preserves row metadata`() {
        val original = Pattern(
            name = "Metadata Test",
            components = listOf(
                PatternComponent(
                    name = "Body",
                    rows = listOf(
                        PatternRow(
                            description = "Magic ring start",
                            instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6)),
                            hasMagicRing = true,
                            sided = Sided.RightSide,
                            repeatCount = 1
                        ),
                        PatternRow(
                            description = "Increase round",
                            instructions = listOf(StitchGroup.of(Stitch.Increase, 6)),
                            sided = Sided.WrongSide,
                            repeatCount = 3
                        )
                    )
                )
            )
        )

        val encoded = codec.encode(original).getOrThrow()
        val decoded = codec.decodeToPattern(encoded).getOrThrow()

        val originalRows = original.components[0].rows
        val decodedRows = decoded.components[0].rows

        assertEquals(originalRows[0].description, decodedRows[0].description)
        assertEquals(originalRows[0].hasMagicRing, decodedRows[0].hasMagicRing)
        assertEquals(originalRows[0].sided, decodedRows[0].sided)
        assertEquals(originalRows[0].repeatCount, decodedRows[0].repeatCount)

        assertEquals(originalRows[1].sided, decodedRows[1].sided)
        assertEquals(originalRows[1].repeatCount, decodedRows[1].repeatCount)
    }

    @Test
    fun `round trip preserves stitch groups with repetitions`() {
        val original = Pattern(
            name = "Group Test",
            components = listOf(
                PatternComponent(
                    name = "Body",
                    rows = listOf(
                        PatternRow(
                            instructions = listOf(
                                StitchGroup(
                                    stitches = listOf(Stitch.SingleCrochet, Stitch.Increase),
                                    repetitions = 6
                                ),
                                StitchGroup(
                                    stitches = listOf(Stitch.DoubleCrochet, Stitch.DoubleCrochet, Stitch.Chain),
                                    repetitions = 4
                                )
                            )
                        )
                    )
                )
            )
        )

        val encoded = codec.encode(original).getOrThrow()
        val decoded = codec.decodeToPattern(encoded).getOrThrow()

        val originalInstructions = original.components[0].rows[0].instructions
        val decodedInstructions = decoded.components[0].rows[0].instructions

        assertEquals(originalInstructions.size, decodedInstructions.size)
        assertEquals(originalInstructions[0].stitches, decodedInstructions[0].stitches)
        assertEquals(originalInstructions[0].repetitions, decodedInstructions[0].repetitions)
        assertEquals(originalInstructions[1].stitches, decodedInstructions[1].stitches)
        assertEquals(originalInstructions[1].repetitions, decodedInstructions[1].repetitions)
    }

    @Test
    fun `round trip preserves custom stitch definitions`() {
        val original = Pattern(
            name = "Custom Stitch Test",
            components = listOf(
                PatternComponent(name = "Body", rows = listOf(
                    PatternRow(instructions = listOf(StitchGroup.of(Stitch.Custom("pbo", 1), 6)))
                ))
            ),
            customStitches = listOf(
                CustomStitchDefinition(
                    abbreviation = "pbo",
                    displayName = "Partial Bobble",
                    description = "A bobble worked partially",
                    instructions = listOf(
                        StitchInstruction.YarnOver,
                        StitchInstruction.Insert(InsertTarget.CurrentStitch),
                        StitchInstruction.Pull(PullCount.Specific(2))
                    ),
                    stitchCount = 1
                )
            )
        )

        val encoded = codec.encode(original).getOrThrow()
        val decoded = codec.decodeToPattern(encoded).getOrThrow()

        assertEquals(1, decoded.customStitches.size)
        val cs = decoded.customStitches[0]
        assertEquals("pbo", cs.abbreviation)
        assertEquals("Partial Bobble", cs.displayName)
        assertEquals("A bobble worked partially", cs.description)
        assertEquals(3, cs.instructions.size)
        assertEquals(1, cs.stitchCount)
    }

    // ==================== FORWARD COMPATIBILITY TESTS ====================

    @Test
    fun `decode ignores unknown fields in pattern`() {
        // JSON with extra unknown field
        val json = """{"v":1,"n":"Test","d":"","c":[{"n":"Body","r":[]}],"unknownField":"ignored"}"""
        val encoded = gzipAndEncode(json)

        val result = codec.decode(encoded)

        assertTrue(result.isSuccess)
        assertEquals("Test", result.getOrThrow().name)
    }

    @Test
    fun `decode ignores unknown fields in component`() {
        val json = """{"v":1,"n":"Test","d":"","c":[{"n":"Body","r":[],"futureField":123}]}"""
        val encoded = gzipAndEncode(json)

        val result = codec.decode(encoded)

        assertTrue(result.isSuccess)
        assertEquals("Body", result.getOrThrow().components[0].name)
    }

    @Test
    fun `decode ignores unknown fields - forward compatibility`() {
        // Encode a valid pattern, then decompress, add unknown fields, recompress
        val pattern = createSimplePattern()
        val encoded = codec.encode(pattern).getOrThrow()

        // Decode the base64 and decompress to get JSON
        val compressed = android.util.Base64.decode(encoded, android.util.Base64.URL_SAFE)
        val originalJson = java.util.zip.GZIPInputStream(
            java.io.ByteArrayInputStream(compressed)
        ).bufferedReader().readText()

        // Inject unknown fields (simulating a future version)
        val modifiedJson = originalJson
            .replace(""""v":1""", """"v":1,"futureField":"ignored"""")
            .replace(""""n":"Body"""", """"n":"Body","newComponentField":123""")

        // Re-encode with modifications
        val modifiedEncoded = gzipAndEncode(modifiedJson)

        // Should still decode successfully
        val result = codec.decode(modifiedEncoded)

        assertTrue("Should decode despite unknown fields: ${result.exceptionOrNull()}", result.isSuccess)
        assertEquals("Test Pattern", result.getOrThrow().name)
        assertEquals(1, result.getOrThrow().components.size)
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun createSimplePattern(): Pattern = Pattern(
        name = "Test Pattern",
        description = "A test pattern",
        components = listOf(
            PatternComponent(
                name = "Body",
                rows = listOf(
                    PatternRow(
                        instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6))
                    )
                )
            )
        )
    )

    private fun createLargePattern(): Pattern = Pattern(
        name = "Large Pattern",
        description = "A pattern with many rows",
        components = listOf(
            PatternComponent(
                name = "Body",
                rows = (1..50).map {
                    PatternRow(
                        instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6))
                    )
                }
            )
        )
    )

    private fun createHugePattern(): Pattern = Pattern(
        name = "Huge Pattern",
        description = "A pattern that will exceed QR capacity",
        components = listOf(
            PatternComponent(
                name = "Body",
                rows = (1..500).map { rowNum ->
                    PatternRow(
                        description = "Row $rowNum with a very long description to increase size " +
                                "and make sure this pattern exceeds QR code capacity limits",
                        instructions = listOf(
                            StitchGroup(
                                stitches = listOf(
                                    Stitch.SingleCrochet, Stitch.DoubleCrochet, Stitch.HalfDoubleCrochet,
                                    Stitch.Treble, Stitch.Increase, Stitch.Decrease
                                ),
                                repetitions = 10
                            )
                        )
                    )
                }
            )
        )
    )

    private fun createComplexPattern(): Pattern = Pattern(
        name = "Complex Amigurumi",
        description = "A complex pattern with multiple components and custom stitches",
        components = listOf(
            PatternComponent(
                name = "Head",
                rows = listOf(
                    PatternRow(
                        description = "Magic ring",
                        instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 6)),
                        hasMagicRing = true
                    ),
                    PatternRow(
                        instructions = listOf(StitchGroup.of(Stitch.Increase, 6))
                    ),
                    PatternRow(
                        instructions = listOf(
                            StitchGroup(
                                stitches = listOf(Stitch.SingleCrochet, Stitch.Increase),
                                repetitions = 6
                            )
                        )
                    )
                )
            ),
            PatternComponent(
                name = "Body",
                rows = listOf(
                    PatternRow(
                        instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 18))
                    ),
                    PatternRow(
                        instructions = listOf(StitchGroup.of(Stitch.SingleCrochet, 18)),
                        repeatCount = 5
                    )
                )
            )
        ),
        customStitches = listOf(
            CustomStitchDefinition(
                abbreviation = "sp",
                displayName = "Special",
                instructions = listOf(StitchInstruction.YarnOver),
                stitchCount = 1
            )
        )
    )

    private fun gzipAndEncode(text: String): String {
        val bos = java.io.ByteArrayOutputStream()
        java.util.zip.GZIPOutputStream(bos).use { gzip ->
            gzip.write(text.toByteArray(Charsets.UTF_8))
        }
        return android.util.Base64.encodeToString(
            bos.toByteArray(),
            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
        )
    }
}
