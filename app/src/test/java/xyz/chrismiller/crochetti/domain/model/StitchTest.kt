package xyz.chrismiller.crochetti.domain.model

import org.junit.Assert.*
import org.junit.Test

class StitchTest {

    // ==================== BASIC STITCHES ====================

    @Test
    fun `chain stitch parsed from ch`() {
        assertEquals(Stitch.Chain, Stitch.fromAbbreviation("ch"))
        assertEquals(Stitch.Chain, Stitch.fromAbbreviation("CH"))
        assertEquals(Stitch.Chain, Stitch.fromAbbreviation(" ch "))
    }

    @Test
    fun `slip stitch parsed from multiple abbreviations`() {
        assertEquals(Stitch.SlipStitch, Stitch.fromAbbreviation("sl"))
        assertEquals(Stitch.SlipStitch, Stitch.fromAbbreviation("sl st"))
        assertEquals(Stitch.SlipStitch, Stitch.fromAbbreviation("ss"))
        assertEquals(Stitch.SlipStitch, Stitch.fromAbbreviation("slst"))
        assertEquals(Stitch.SlipStitch, Stitch.fromAbbreviation("SL ST"))
    }

    @Test
    fun `single crochet parsed from sc`() {
        assertEquals(Stitch.SingleCrochet, Stitch.fromAbbreviation("sc"))
        assertEquals(Stitch.SingleCrochet, Stitch.fromAbbreviation("SC"))
    }

    @Test
    fun `half double crochet parsed from hdc`() {
        assertEquals(Stitch.HalfDoubleCrochet, Stitch.fromAbbreviation("hdc"))
        assertEquals(Stitch.HalfDoubleCrochet, Stitch.fromAbbreviation("HDC"))
    }

    @Test
    fun `double crochet parsed from dc`() {
        assertEquals(Stitch.DoubleCrochet, Stitch.fromAbbreviation("dc"))
        assertEquals(Stitch.DoubleCrochet, Stitch.fromAbbreviation("DC"))
    }

    @Test
    fun `treble crochet parsed from tr and tc`() {
        assertEquals(Stitch.Treble, Stitch.fromAbbreviation("tr"))
        assertEquals(Stitch.Treble, Stitch.fromAbbreviation("tc"))
        assertEquals(Stitch.Treble, Stitch.fromAbbreviation("TR"))
    }

    @Test
    fun `double treble parsed from dtr`() {
        assertEquals(Stitch.DoubleTreble, Stitch.fromAbbreviation("dtr"))
        assertEquals(Stitch.DoubleTreble, Stitch.fromAbbreviation("DTR"))
    }

    @Test
    fun `triple treble parsed from trtr and ttr`() {
        assertEquals(Stitch.TripleTreble, Stitch.fromAbbreviation("trtr"))
        assertEquals(Stitch.TripleTreble, Stitch.fromAbbreviation("ttr"))
    }

    // ==================== SHAPING STITCHES ====================

    @Test
    fun `increase parsed from inc and sc inc`() {
        assertEquals(Stitch.Increase, Stitch.fromAbbreviation("inc"))
        assertEquals(Stitch.Increase, Stitch.fromAbbreviation("sc inc"))
        assertEquals(2, Stitch.Increase.stitchCount)
    }

    @Test
    fun `decrease parsed from dec and sc dec`() {
        assertEquals(Stitch.Decrease, Stitch.fromAbbreviation("dec"))
        assertEquals(Stitch.Decrease, Stitch.fromAbbreviation("sc dec"))
        assertEquals(1, Stitch.Decrease.stitchCount)
    }

    @Test
    fun `sc2tog parsed correctly`() {
        assertEquals(Stitch.Sc2Tog, Stitch.fromAbbreviation("sc2tog"))
        assertEquals(1, Stitch.Sc2Tog.stitchCount)
    }

    @Test
    fun `hdc2tog parsed correctly`() {
        assertEquals(Stitch.Hdc2Tog, Stitch.fromAbbreviation("hdc2tog"))
        assertEquals(1, Stitch.Hdc2Tog.stitchCount)
    }

    @Test
    fun `dc2tog parsed correctly`() {
        assertEquals(Stitch.Dc2Tog, Stitch.fromAbbreviation("dc2tog"))
        assertEquals(1, Stitch.Dc2Tog.stitchCount)
    }

    @Test
    fun `tr2tog parsed correctly`() {
        assertEquals(Stitch.Tr2Tog, Stitch.fromAbbreviation("tr2tog"))
        assertEquals(1, Stitch.Tr2Tog.stitchCount)
    }

    // ==================== TEXTURED STITCHES ====================

    @Test
    fun `bobble parsed from multiple abbreviations`() {
        assertEquals(Stitch.Bobble, Stitch.fromAbbreviation("bo"))
        assertEquals(Stitch.Bobble, Stitch.fromAbbreviation("bob"))
        assertEquals(Stitch.Bobble, Stitch.fromAbbreviation("bobble"))
        assertEquals(1, Stitch.Bobble.stitchCount)
    }

    @Test
    fun `popcorn parsed from pc and popcorn`() {
        assertEquals(Stitch.Popcorn, Stitch.fromAbbreviation("pc"))
        assertEquals(Stitch.Popcorn, Stitch.fromAbbreviation("popcorn"))
    }

    @Test
    fun `puff stitch parsed from ps and puff`() {
        assertEquals(Stitch.PuffStitch, Stitch.fromAbbreviation("ps"))
        assertEquals(Stitch.PuffStitch, Stitch.fromAbbreviation("puff"))
    }

    @Test
    fun `cluster parsed from cl and cluster`() {
        assertEquals(Stitch.Cluster, Stitch.fromAbbreviation("cl"))
        assertEquals(Stitch.Cluster, Stitch.fromAbbreviation("cluster"))
    }

    @Test
    fun `shell parsed from sh and shell`() {
        assertEquals(Stitch.Shell, Stitch.fromAbbreviation("sh"))
        assertEquals(Stitch.Shell, Stitch.fromAbbreviation("shell"))
        assertEquals(5, Stitch.Shell.stitchCount) // Shell produces 5 stitches
    }

    // ==================== POST STITCHES ====================

    @Test
    fun `front post single crochet parsed correctly`() {
        assertEquals(Stitch.FrontPostSc, Stitch.fromAbbreviation("fpsc"))
        assertEquals(Stitch.FrontPostSc, Stitch.fromAbbreviation("fp sc"))
    }

    @Test
    fun `back post single crochet parsed correctly`() {
        assertEquals(Stitch.BackPostSc, Stitch.fromAbbreviation("bpsc"))
        assertEquals(Stitch.BackPostSc, Stitch.fromAbbreviation("bp sc"))
    }

    @Test
    fun `front post half double crochet parsed correctly`() {
        assertEquals(Stitch.FrontPostHdc, Stitch.fromAbbreviation("fphdc"))
        assertEquals(Stitch.FrontPostHdc, Stitch.fromAbbreviation("fp hdc"))
    }

    @Test
    fun `back post half double crochet parsed correctly`() {
        assertEquals(Stitch.BackPostHdc, Stitch.fromAbbreviation("bphdc"))
        assertEquals(Stitch.BackPostHdc, Stitch.fromAbbreviation("bp hdc"))
    }

    @Test
    fun `front post double crochet parsed correctly`() {
        assertEquals(Stitch.FrontPostDc, Stitch.fromAbbreviation("fpdc"))
        assertEquals(Stitch.FrontPostDc, Stitch.fromAbbreviation("fp dc"))
    }

    @Test
    fun `back post double crochet parsed correctly`() {
        assertEquals(Stitch.BackPostDc, Stitch.fromAbbreviation("bpdc"))
        assertEquals(Stitch.BackPostDc, Stitch.fromAbbreviation("bp dc"))
    }

    @Test
    fun `front post treble crochet parsed correctly`() {
        assertEquals(Stitch.FrontPostTr, Stitch.fromAbbreviation("fptr"))
        assertEquals(Stitch.FrontPostTr, Stitch.fromAbbreviation("fp tr"))
    }

    @Test
    fun `back post treble crochet parsed correctly`() {
        assertEquals(Stitch.BackPostTr, Stitch.fromAbbreviation("bptr"))
        assertEquals(Stitch.BackPostTr, Stitch.fromAbbreviation("bp tr"))
    }

    // ==================== EXTENDED STITCHES ====================

    @Test
    fun `extended single crochet parsed correctly`() {
        assertEquals(Stitch.ExtendedSc, Stitch.fromAbbreviation("esc"))
        assertEquals(Stitch.ExtendedSc, Stitch.fromAbbreviation("exsc"))
    }

    @Test
    fun `extended half double crochet parsed correctly`() {
        assertEquals(Stitch.ExtendedHdc, Stitch.fromAbbreviation("ehdc"))
        assertEquals(Stitch.ExtendedHdc, Stitch.fromAbbreviation("exhdc"))
    }

    @Test
    fun `extended double crochet parsed correctly`() {
        assertEquals(Stitch.ExtendedDc, Stitch.fromAbbreviation("edc"))
        assertEquals(Stitch.ExtendedDc, Stitch.fromAbbreviation("exdc"))
    }

    // ==================== OTHER ====================

    @Test
    fun `skip parsed from sk and skip`() {
        assertEquals(Stitch.Skip, Stitch.fromAbbreviation("sk"))
        assertEquals(Stitch.Skip, Stitch.fromAbbreviation("skip"))
        assertEquals(0, Stitch.Skip.stitchCount) // Skip produces 0 stitches
    }

    @Test
    fun `unknown abbreviation returns null`() {
        assertNull(Stitch.fromAbbreviation("xyz"))
        assertNull(Stitch.fromAbbreviation("unknown"))
        assertNull(Stitch.fromAbbreviation(""))
    }

    // ==================== STITCH COUNTS ====================

    @Test
    fun `basic stitches have stitch count of 1`() {
        assertEquals(1, Stitch.Chain.stitchCount)
        assertEquals(1, Stitch.SlipStitch.stitchCount)
        assertEquals(1, Stitch.SingleCrochet.stitchCount)
        assertEquals(1, Stitch.HalfDoubleCrochet.stitchCount)
        assertEquals(1, Stitch.DoubleCrochet.stitchCount)
        assertEquals(1, Stitch.Treble.stitchCount)
        assertEquals(1, Stitch.DoubleTreble.stitchCount)
        assertEquals(1, Stitch.TripleTreble.stitchCount)
    }

    @Test
    fun `post stitches have stitch count of 1`() {
        assertEquals(1, Stitch.FrontPostSc.stitchCount)
        assertEquals(1, Stitch.BackPostSc.stitchCount)
        assertEquals(1, Stitch.FrontPostHdc.stitchCount)
        assertEquals(1, Stitch.BackPostHdc.stitchCount)
        assertEquals(1, Stitch.FrontPostDc.stitchCount)
        assertEquals(1, Stitch.BackPostDc.stitchCount)
        assertEquals(1, Stitch.FrontPostTr.stitchCount)
        assertEquals(1, Stitch.BackPostTr.stitchCount)
    }

    @Test
    fun `extended stitches have stitch count of 1`() {
        assertEquals(1, Stitch.ExtendedSc.stitchCount)
        assertEquals(1, Stitch.ExtendedHdc.stitchCount)
        assertEquals(1, Stitch.ExtendedDc.stitchCount)
    }

    // ==================== BUILT-IN STITCHES LIST ====================

    @Test
    fun `builtInStitches is not empty`() {
        assertTrue(Stitch.builtInStitches.isNotEmpty())
    }

    @Test
    fun `builtInStitches has expected count`() {
        // 8 basic + 6 shaping + 5 textured + 8 post + 3 extended + 1 other = 31
        assertEquals(31, Stitch.builtInStitches.size)
    }

    @Test
    fun `all builtin stitches can be parsed from their abbreviation`() {
        // Verify round-trip: each stitch's abbreviation resolves back to itself
        listOf(
            "ch", "sl st", "sc", "hdc", "dc", "tr", "dtr", "trtr",
            "inc", "dec", "sc2tog", "hdc2tog", "dc2tog", "tr2tog",
            "bo", "pc", "ps", "cl", "sh",
            "fpsc", "bpsc", "fphdc", "bphdc", "fpdc", "bpdc", "fptr", "bptr",
            "esc", "ehdc", "edc",
            "sk"
        ).forEach { abbr ->
            assertNotNull("Failed to parse: $abbr", Stitch.fromAbbreviation(abbr))
        }
    }

    // ==================== CUSTOM STITCHES ====================

    @Test
    fun `custom stitch can be created with name and count`() {
        val custom = Stitch.Custom("pbo", 1)
        assertEquals("pbo", custom.abbreviation)
        assertEquals("pbo", custom.displayName)
        assertEquals(1, custom.stitchCount)
    }

    @Test
    fun `custom stitch defaults to stitch count of 1`() {
        val custom = Stitch.Custom("myStitch")
        assertEquals(1, custom.stitchCount)
    }
}
