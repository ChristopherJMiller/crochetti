package xyz.chrismiller.crochetti.domain.parser

import org.junit.Assert.*
import org.junit.Test
import xyz.chrismiller.crochetti.domain.model.Stitch

class PatternDslParserTest {

    private val parser = PatternDslParser()

    // ==================== BASIC PARSING ====================

    @Test
    fun `empty string returns empty list`() {
        val result = parser.parse("")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        assertEquals(0, (result as PatternDslParser.ParseResult.Success).groups.size)
    }

    @Test
    fun `blank string returns empty list`() {
        val result = parser.parse("   ")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        assertEquals(0, (result as PatternDslParser.ParseResult.Success).groups.size)
    }

    @Test
    fun `single stitch parsed correctly`() {
        val result = parser.parse("sc")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(1, groups.size)
        assertEquals(1, groups[0].stitches.size)
        assertEquals(Stitch.SingleCrochet, groups[0].stitches[0])
    }

    @Test
    fun `stitch with count parsed correctly`() {
        val result = parser.parse("sc 6")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(1, groups.size)
        assertEquals(6, groups[0].stitches.size)
        assertTrue(groups[0].stitches.all { it == Stitch.SingleCrochet })
    }

    @Test
    fun `multiple stitches with comma separated`() {
        val result = parser.parse("sc 2, inc")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(2, groups.size)
        assertEquals(2, groups[0].stitches.size)
        assertEquals(1, groups[1].stitches.size)
        assertEquals(Stitch.Increase, groups[1].stitches[0])
    }

    // ==================== GROUPING AND REPETITION ====================

    @Test
    fun `group with repetition parsed correctly`() {
        val result = parser.parse("(sc 2, inc) x6")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(1, groups.size)
        assertEquals(6, groups[0].repetitions)
        // Group contains 2 sc + 1 inc = 3 stitches
        assertEquals(3, groups[0].stitches.size)
    }

    @Test
    fun `mixed single and group`() {
        val result = parser.parse("sc 6, (sc 2, inc) x6")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(2, groups.size)
        assertEquals(6, groups[0].stitches.size) // 6 sc
        assertEquals(6, groups[1].repetitions) // repeated 6 times
    }

    // ==================== NEW STITCH TYPES ====================

    @Test
    fun `bobble stitch parsed correctly`() {
        val result = parser.parse("sc, bo, sc")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(3, groups.size)
        assertEquals(Stitch.Bobble, groups[1].stitches[0])
    }

    @Test
    fun `popcorn stitch parsed correctly`() {
        val result = parser.parse("pc 3")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(1, groups.size)
        assertEquals(3, groups[0].stitches.size)
        assertTrue(groups[0].stitches.all { it == Stitch.Popcorn })
    }

    @Test
    fun `puff stitch parsed correctly`() {
        val result = parser.parse("ps, sc 2")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(2, groups.size)
        assertEquals(Stitch.PuffStitch, groups[0].stitches[0])
    }

    @Test
    fun `cluster stitch parsed correctly`() {
        val result = parser.parse("cl 2, ch")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(2, groups.size)
        assertEquals(2, groups[0].stitches.size)
        assertTrue(groups[0].stitches.all { it == Stitch.Cluster })
    }

    @Test
    fun `shell stitch parsed correctly`() {
        val result = parser.parse("sh, sk, sh")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(3, groups.size)
        assertEquals(Stitch.Shell, groups[0].stitches[0])
        assertEquals(Stitch.Skip, groups[1].stitches[0])
        assertEquals(Stitch.Shell, groups[2].stitches[0])
    }

    // ==================== POST STITCHES ====================

    @Test
    fun `front post dc parsed correctly with space`() {
        val result = parser.parse("fp dc 2")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(1, groups.size)
        assertEquals(2, groups[0].stitches.size)
        assertTrue(groups[0].stitches.all { it == Stitch.FrontPostDc })
    }

    @Test
    fun `back post dc parsed correctly without space`() {
        val result = parser.parse("bpdc 3")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(1, groups.size)
        assertEquals(3, groups[0].stitches.size)
        assertTrue(groups[0].stitches.all { it == Stitch.BackPostDc })
    }

    @Test
    fun `ribbing pattern with front and back post`() {
        val result = parser.parse("(fpdc, bpdc) x10")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(1, groups.size)
        assertEquals(10, groups[0].repetitions)
        assertEquals(2, groups[0].stitches.size)
        assertEquals(Stitch.FrontPostDc, groups[0].stitches[0])
        assertEquals(Stitch.BackPostDc, groups[0].stitches[1])
    }

    @Test
    fun `front post hdc with space parsed correctly`() {
        val result = parser.parse("fp hdc 2")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(1, groups.size)
        assertTrue(groups[0].stitches.all { it == Stitch.FrontPostHdc })
    }

    // ==================== EXTENDED STITCHES ====================

    @Test
    fun `extended single crochet parsed correctly`() {
        val result = parser.parse("esc 6")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(1, groups.size)
        assertEquals(6, groups[0].stitches.size)
        assertTrue(groups[0].stitches.all { it == Stitch.ExtendedSc })
    }

    @Test
    fun `extended half double crochet parsed correctly`() {
        val result = parser.parse("ehdc 4")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(1, groups.size)
        assertTrue(groups[0].stitches.all { it == Stitch.ExtendedHdc })
    }

    // ==================== DECREASE STITCHES ====================

    @Test
    fun `sc2tog parsed correctly`() {
        val result = parser.parse("sc 4, sc2tog")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(2, groups.size)
        assertEquals(Stitch.Sc2Tog, groups[1].stitches[0])
    }

    @Test
    fun `dc2tog parsed correctly`() {
        val result = parser.parse("dc2tog 2")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(1, groups.size)
        assertEquals(2, groups[0].stitches.size)
        assertTrue(groups[0].stitches.all { it == Stitch.Dc2Tog })
    }

    // ==================== TALL STITCHES ====================

    @Test
    fun `double treble parsed correctly`() {
        val result = parser.parse("dtr 3")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(1, groups.size)
        assertTrue(groups[0].stitches.all { it == Stitch.DoubleTreble })
    }

    @Test
    fun `triple treble parsed correctly`() {
        val result = parser.parse("trtr 2")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(1, groups.size)
        assertTrue(groups[0].stitches.all { it == Stitch.TripleTreble })
    }

    // ==================== SLIP STITCH ====================

    @Test
    fun `slip stitch with space parsed correctly`() {
        val result = parser.parse("sl st 2")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(1, groups.size)
        assertEquals(2, groups[0].stitches.size)
        assertTrue(groups[0].stitches.all { it == Stitch.SlipStitch })
    }

    // ==================== COMPLEX PATTERNS ====================

    @Test
    fun `amigurumi round pattern`() {
        // Typical amigurumi round: sc 6, (sc, inc) x6 = 18 stitches
        val result = parser.parse("sc 6, (sc, inc) x6")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(2, groups.size)

        // First group: 6 sc
        assertEquals(6, groups[0].stitches.size)
        assertEquals(1, groups[0].repetitions)

        // Second group: (sc, inc) repeated 6 times
        assertEquals(2, groups[1].stitches.size)
        assertEquals(6, groups[1].repetitions)
    }

    @Test
    fun `cable pattern with post stitches`() {
        val result = parser.parse("sc 2, fpdc 4, sc 2, bpdc 4")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(4, groups.size)
    }

    @Test
    fun `textured pattern with bobbles`() {
        val result = parser.parse("(sc 3, bo) x4, sc 3")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        assertEquals(2, groups.size)
    }

    // ==================== ERROR HANDLING ====================

    @Test
    fun `unknown stitch returns error`() {
        val result = parser.parse("xyz 6")
        assertTrue(result is PatternDslParser.ParseResult.Error)
        assertTrue((result as PatternDslParser.ParseResult.Error).message.contains("Unknown stitch"))
    }

    @Test
    fun `unclosed parenthesis returns error`() {
        val result = parser.parse("(sc 2, inc")
        assertTrue(result is PatternDslParser.ParseResult.Error)
    }

    @Test
    fun `missing number after x returns error`() {
        val result = parser.parse("(sc 2, inc) x")
        assertTrue(result is PatternDslParser.ParseResult.Error)
    }

    // ==================== STITCH COUNT CALCULATION ====================

    @Test
    fun `total stitch count calculated correctly for simple pattern`() {
        val result = parser.parse("sc 6")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        val total = groups.sumOf { it.totalStitchCount }
        assertEquals(6, total)
    }

    @Test
    fun `total stitch count with increase accounts for 2 stitches`() {
        val result = parser.parse("sc 5, inc")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        val total = groups.sumOf { it.totalStitchCount }
        // 5 sc (1 each) + 1 inc (2) = 7 stitches
        assertEquals(7, total)
    }

    @Test
    fun `total stitch count with skip accounts for 0 stitches`() {
        val result = parser.parse("sc 5, sk, sc")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        val total = groups.sumOf { it.totalStitchCount }
        // 5 sc + 0 skip + 1 sc = 6 stitches
        assertEquals(6, total)
    }

    @Test
    fun `total stitch count with shell accounts for 5 stitches`() {
        val result = parser.parse("sc, sh, sc")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        val total = groups.sumOf { it.totalStitchCount }
        // 1 sc + 5 shell + 1 sc = 7 stitches
        assertEquals(7, total)
    }

    @Test
    fun `total stitch count with repetition`() {
        val result = parser.parse("(sc 2, inc) x6")
        assertTrue(result is PatternDslParser.ParseResult.Success)
        val groups = (result as PatternDslParser.ParseResult.Success).groups
        val total = groups.sumOf { it.totalStitchCount }
        // (2 sc + 1 inc(2)) x 6 = 4 x 6 = 24 stitches
        assertEquals(24, total)
    }

    // ==================== CONVENIENCE FUNCTIONS ====================

    @Test
    fun `parsePattern static function works`() {
        val result = PatternDslParser.parsePattern("sc 6")
        assertTrue(result is PatternDslParser.ParseResult.Success)
    }

    @Test
    fun `parseOrNull returns groups on success`() {
        val groups = PatternDslParser.parseOrNull("sc 6")
        assertNotNull(groups)
        assertEquals(1, groups!!.size)
    }

    @Test
    fun `parseOrNull returns null on error`() {
        val groups = PatternDslParser.parseOrNull("xyz 6")
        assertNull(groups)
    }
}
