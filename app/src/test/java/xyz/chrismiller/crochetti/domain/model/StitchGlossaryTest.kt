package xyz.chrismiller.crochetti.domain.model

import org.junit.Assert.*
import org.junit.Test

class StitchGlossaryTest {

    @Test
    fun `all expected stitches have glossary entries`() {
        val glossaryAbbreviations = StitchGlossary.allStitches.map { it.stitch.abbreviation }.toSet()

        // Check that all expected abbreviations have glossary entries
        listOf(
            "ch", "sl st", "sc", "hdc", "dc", "tr", "dtr", "trtr",
            "inc", "dec", "sc2tog", "hdc2tog", "dc2tog", "tr2tog",
            "bo", "pc", "ps", "cl", "sh",
            "fpsc", "bpsc", "fphdc", "bphdc", "fpdc", "bpdc", "fptr", "bptr",
            "esc", "ehdc", "edc",
            "sk"
        ).forEach { abbr ->
            assertTrue(
                "Missing glossary entry for $abbr",
                abbr in glossaryAbbreviations
            )
        }
    }

    @Test
    fun `glossary has expected stitch count`() {
        // 8 basic + 6 shaping + 5 textured + 8 post + 3 extended + 1 other = 31
        assertEquals(31, StitchGlossary.allStitches.size)
    }

    @Test
    fun `all categories have at least one stitch`() {
        StitchCategory.entries.forEach { category ->
            val stitches = StitchGlossary.getByCategory(category)
            assertTrue(
                "Category ${category.displayName} has no stitches",
                stitches.isNotEmpty()
            )
        }
    }

    @Test
    fun `groupedByCategory returns all stitches`() {
        val grouped = StitchGlossary.groupedByCategory()
        val totalCount = grouped.values.sumOf { it.size }
        assertEquals(StitchGlossary.allStitches.size, totalCount)
    }

    @Test
    fun `search finds stitch by name`() {
        val results = StitchGlossary.search("single crochet")
        assertTrue(results.any { it.stitch == Stitch.SingleCrochet })
    }

    @Test
    fun `search finds stitch by abbreviation`() {
        val results = StitchGlossary.search("sc")
        assertTrue(results.any { it.stitch == Stitch.SingleCrochet })
    }

    @Test
    fun `search is case insensitive`() {
        val results = StitchGlossary.search("BOBBLE")
        assertTrue(results.any { it.stitch == Stitch.Bobble })
    }

    @Test
    fun `search with empty query returns all stitches`() {
        val results = StitchGlossary.search("")
        assertEquals(StitchGlossary.allStitches.size, results.size)
    }

    @Test
    fun `search with blank query returns all stitches`() {
        val results = StitchGlossary.search("   ")
        assertEquals(StitchGlossary.allStitches.size, results.size)
    }

    @Test
    fun `getInfoForStitches returns info for list of stitches`() {
        val stitches = listOf(
            Stitch.SingleCrochet,
            Stitch.Increase,
            Stitch.DoubleCrochet
        )
        val infos = StitchGlossary.getInfoForStitches(stitches)
        assertEquals(3, infos.size)
    }

    @Test
    fun `getInfoForStitches removes duplicates`() {
        val stitches = listOf(
            Stitch.SingleCrochet,
            Stitch.SingleCrochet,
            Stitch.SingleCrochet
        )
        val infos = StitchGlossary.getInfoForStitches(stitches)
        assertEquals(1, infos.size)
    }

    @Test
    fun `all stitch infos have non-empty descriptions`() {
        StitchGlossary.allStitches.forEach { info ->
            assertTrue(
                "${info.stitch.displayName} has empty description",
                info.description.isNotBlank()
            )
        }
    }

    @Test
    fun `all stitch infos have non-empty instructions`() {
        StitchGlossary.allStitches.forEach { info ->
            assertTrue(
                "${info.stitch.displayName} has empty instructions",
                info.instructions.isNotBlank()
            )
        }
    }

    @Test
    fun `basic stitches are beginner difficulty`() {
        val basicStitches = StitchGlossary.getByCategory(StitchCategory.BASIC)
        val beginnerStitches = basicStitches.filter { it.difficulty == StitchDifficulty.BEGINNER }
        // Most basic stitches should be beginner level
        assertTrue(beginnerStitches.size >= 6)
    }

    @Test
    fun `post stitches are at least intermediate difficulty`() {
        val postStitches = StitchGlossary.getByCategory(StitchCategory.POST)
        postStitches.forEach { info ->
            assertTrue(
                "${info.stitch.displayName} should be at least intermediate",
                info.difficulty != StitchDifficulty.BEGINNER
            )
        }
    }

    @Test
    fun `shaping category contains increase and decrease`() {
        val shapingStitches = StitchGlossary.getByCategory(StitchCategory.SHAPING)
        assertTrue(shapingStitches.any { it.stitch == Stitch.Increase })
        assertTrue(shapingStitches.any { it.stitch == Stitch.Decrease })
    }

    @Test
    fun `textured category contains bobble popcorn and puff`() {
        val texturedStitches = StitchGlossary.getByCategory(StitchCategory.TEXTURED)
        assertTrue(texturedStitches.any { it.stitch == Stitch.Bobble })
        assertTrue(texturedStitches.any { it.stitch == Stitch.Popcorn })
        assertTrue(texturedStitches.any { it.stitch == Stitch.PuffStitch })
    }
}
