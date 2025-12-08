package xyz.chrismiller.crochetti.domain.model

/**
 * Glossary of all supported crochet stitches with descriptions and instructions.
 * Based on Craft Yarn Council standards.
 */
object StitchGlossary {

    private val stitchInfoMap: Map<Stitch, StitchInfo> = buildMap {
        // ==================== BASIC STITCHES ====================

        put(
            Stitch.Chain,
            StitchInfo(
                stitch = Stitch.Chain,
                category = StitchCategory.BASIC,
                description = "The foundation of most crochet projects. Creates a series of interlocking loops.",
                instructions = "Yarn over, pull through loop on hook.",
                difficulty = StitchDifficulty.BEGINNER,
                aliases = listOf("ch"),
                ukEquivalent = "Chain"
            )
        )

        put(
            Stitch.SlipStitch,
            StitchInfo(
                stitch = Stitch.SlipStitch,
                category = StitchCategory.BASIC,
                description = "The shortest crochet stitch. Used to join rounds, move yarn, or create a flat seam.",
                instructions = "Insert hook in stitch, yarn over, pull through both stitch and loop on hook in one motion.",
                difficulty = StitchDifficulty.BEGINNER,
                aliases = listOf("sl st", "sl", "ss", "slst"),
                ukEquivalent = "Slip Stitch"
            )
        )

        put(
            Stitch.SingleCrochet,
            StitchInfo(
                stitch = Stitch.SingleCrochet,
                category = StitchCategory.BASIC,
                description = "A short, tight stitch that creates a dense, sturdy fabric. Great for amigurumi and dishcloths.",
                instructions = "Insert hook in stitch, yarn over, pull up a loop (2 loops on hook), yarn over, pull through both loops.",
                difficulty = StitchDifficulty.BEGINNER,
                aliases = listOf("sc"),
                ukEquivalent = "Double Crochet (dc)"
            )
        )

        put(
            Stitch.HalfDoubleCrochet,
            StitchInfo(
                stitch = Stitch.HalfDoubleCrochet,
                category = StitchCategory.BASIC,
                description = "Slightly taller than single crochet. Creates a fabric with nice drape.",
                instructions = "Yarn over, insert hook in stitch, yarn over, pull up a loop (3 loops on hook), yarn over, pull through all 3 loops.",
                difficulty = StitchDifficulty.BEGINNER,
                aliases = listOf("hdc"),
                ukEquivalent = "Half Treble Crochet (htr)"
            )
        )

        put(
            Stitch.DoubleCrochet,
            StitchInfo(
                stitch = Stitch.DoubleCrochet,
                category = StitchCategory.BASIC,
                description = "A tall, versatile stitch. Works up quickly and creates an open, drapey fabric.",
                instructions = "Yarn over, insert hook in stitch, yarn over, pull up a loop (3 loops on hook), yarn over, pull through 2 loops (2 loops remain), yarn over, pull through remaining 2 loops.",
                difficulty = StitchDifficulty.BEGINNER,
                aliases = listOf("dc"),
                ukEquivalent = "Treble Crochet (tr)"
            )
        )

        put(
            Stitch.Treble,
            StitchInfo(
                stitch = Stitch.Treble,
                category = StitchCategory.BASIC,
                description = "A very tall stitch that creates an open, lacy fabric. Works up very quickly.",
                instructions = "Yarn over twice, insert hook in stitch, yarn over, pull up a loop (4 loops on hook), [yarn over, pull through 2 loops] 3 times.",
                difficulty = StitchDifficulty.BEGINNER,
                aliases = listOf("tr", "tc"),
                ukEquivalent = "Double Treble Crochet (dtr)"
            )
        )

        put(
            Stitch.DoubleTreble,
            StitchInfo(
                stitch = Stitch.DoubleTreble,
                category = StitchCategory.BASIC,
                description = "An extra tall stitch for very open, lacy work.",
                instructions = "Yarn over 3 times, insert hook in stitch, yarn over, pull up a loop (5 loops on hook), [yarn over, pull through 2 loops] 4 times.",
                difficulty = StitchDifficulty.INTERMEDIATE,
                aliases = listOf("dtr"),
                ukEquivalent = "Triple Treble Crochet (trtr)"
            )
        )

        put(
            Stitch.TripleTreble,
            StitchInfo(
                stitch = Stitch.TripleTreble,
                category = StitchCategory.BASIC,
                description = "The tallest of the basic stitches. Creates very open fabric.",
                instructions = "Yarn over 4 times, insert hook in stitch, yarn over, pull up a loop (6 loops on hook), [yarn over, pull through 2 loops] 5 times.",
                difficulty = StitchDifficulty.INTERMEDIATE,
                aliases = listOf("trtr", "ttr"),
                ukEquivalent = "Quadruple Treble Crochet"
            )
        )

        // ==================== SHAPING STITCHES ====================

        put(
            Stitch.Increase,
            StitchInfo(
                stitch = Stitch.Increase,
                category = StitchCategory.SHAPING,
                description = "Makes your work wider by adding stitches. Work 2 stitches in the same stitch.",
                instructions = "Work 2 single crochet stitches into the same stitch.",
                difficulty = StitchDifficulty.BEGINNER,
                aliases = listOf("inc", "sc inc")
            )
        )

        put(
            Stitch.Decrease,
            StitchInfo(
                stitch = Stitch.Decrease,
                category = StitchCategory.SHAPING,
                description = "Makes your work narrower by combining stitches. Also called invisible decrease.",
                instructions = "Insert hook in front loop of next stitch, insert hook in front loop of following stitch, yarn over, pull through both front loops, yarn over, pull through both loops on hook.",
                difficulty = StitchDifficulty.BEGINNER,
                aliases = listOf("dec", "sc dec", "invdec")
            )
        )

        put(
            Stitch.Sc2Tog,
            StitchInfo(
                stitch = Stitch.Sc2Tog,
                category = StitchCategory.SHAPING,
                description = "Standard single crochet decrease. Combines 2 stitches into 1.",
                instructions = "Insert hook in stitch, yarn over, pull up a loop, insert hook in next stitch, yarn over, pull up a loop (3 loops on hook), yarn over, pull through all 3 loops.",
                difficulty = StitchDifficulty.BEGINNER,
                aliases = listOf("sc2tog")
            )
        )

        put(
            Stitch.Hdc2Tog,
            StitchInfo(
                stitch = Stitch.Hdc2Tog,
                category = StitchCategory.SHAPING,
                description = "Half double crochet decrease. Combines 2 stitches into 1.",
                instructions = "Yarn over, insert hook in stitch, yarn over, pull up a loop, yarn over, insert hook in next stitch, yarn over, pull up a loop (5 loops on hook), yarn over, pull through all 5 loops.",
                difficulty = StitchDifficulty.BEGINNER,
                aliases = listOf("hdc2tog")
            )
        )

        put(
            Stitch.Dc2Tog,
            StitchInfo(
                stitch = Stitch.Dc2Tog,
                category = StitchCategory.SHAPING,
                description = "Double crochet decrease. Combines 2 stitches into 1.",
                instructions = "Yarn over, insert hook in stitch, yarn over, pull up a loop, yarn over, pull through 2 loops (2 loops remain), yarn over, insert hook in next stitch, yarn over, pull up a loop, yarn over, pull through 2 loops (3 loops remain), yarn over, pull through all 3 loops.",
                difficulty = StitchDifficulty.INTERMEDIATE,
                aliases = listOf("dc2tog")
            )
        )

        put(
            Stitch.Tr2Tog,
            StitchInfo(
                stitch = Stitch.Tr2Tog,
                category = StitchCategory.SHAPING,
                description = "Treble crochet decrease. Combines 2 stitches into 1.",
                instructions = "Yarn over twice, insert hook in stitch, yarn over, pull up a loop, [yarn over, pull through 2 loops] twice (2 loops remain), yarn over twice, insert hook in next stitch, yarn over, pull up a loop, [yarn over, pull through 2 loops] twice (3 loops remain), yarn over, pull through all 3 loops.",
                difficulty = StitchDifficulty.INTERMEDIATE,
                aliases = listOf("tr2tog")
            )
        )

        // ==================== TEXTURED STITCHES ====================

        put(
            Stitch.Bobble,
            StitchInfo(
                stitch = Stitch.Bobble,
                category = StitchCategory.TEXTURED,
                description = "A 3D raised bump that adds texture. Creates a decorative bumpy surface that pops to the front of the work.",
                instructions = "[Yarn over, insert hook in stitch, yarn over, pull up a loop, yarn over, pull through 2 loops] 5 times in same stitch (6 loops on hook), yarn over, pull through all 6 loops.",
                difficulty = StitchDifficulty.INTERMEDIATE,
                aliases = listOf("bo", "bob", "bobble")
            )
        )

        put(
            Stitch.Popcorn,
            StitchInfo(
                stitch = Stitch.Popcorn,
                category = StitchCategory.TEXTURED,
                description = "A pronounced 3D stitch that pops forward. More defined than a bobble.",
                instructions = "Work 5 double crochets in same stitch, remove hook from loop, insert hook in first dc of group, pick up dropped loop, pull through (this closes the popcorn).",
                difficulty = StitchDifficulty.INTERMEDIATE,
                aliases = listOf("pc", "popcorn")
            )
        )

        put(
            Stitch.PuffStitch,
            StitchInfo(
                stitch = Stitch.PuffStitch,
                category = StitchCategory.TEXTURED,
                description = "A soft, puffy stitch. Gentler texture than bobble or popcorn.",
                instructions = "[Yarn over, insert hook in stitch, yarn over, pull up a loop to height of half double crochet] 3-5 times in same stitch, yarn over, pull through all loops on hook.",
                difficulty = StitchDifficulty.INTERMEDIATE,
                aliases = listOf("ps", "puff")
            )
        )

        put(
            Stitch.Cluster,
            StitchInfo(
                stitch = Stitch.Cluster,
                category = StitchCategory.TEXTURED,
                description = "Multiple partial stitches joined at the top. Can span multiple stitches or work into one.",
                instructions = "[Yarn over, insert hook in stitch, yarn over, pull up a loop, yarn over, pull through 2 loops] in each of specified stitches, yarn over, pull through all loops on hook.",
                difficulty = StitchDifficulty.INTERMEDIATE,
                aliases = listOf("cl", "cluster")
            )
        )

        put(
            Stitch.Shell,
            StitchInfo(
                stitch = Stitch.Shell,
                category = StitchCategory.TEXTURED,
                description = "Multiple stitches worked into the same stitch, fanning out in a shell shape. Creates a scalloped edge.",
                instructions = "Work 5 double crochets (or specified number) all in the same stitch or space.",
                difficulty = StitchDifficulty.BEGINNER,
                aliases = listOf("sh", "shell")
            )
        )

        // ==================== POST STITCHES ====================

        put(
            Stitch.FrontPostSc,
            StitchInfo(
                stitch = Stitch.FrontPostSc,
                category = StitchCategory.POST,
                description = "Single crochet worked around the post of a stitch from the front. Creates subtle texture.",
                instructions = "Insert hook from front to back to front around the post of the indicated stitch, yarn over, pull up a loop, yarn over, pull through both loops.",
                difficulty = StitchDifficulty.INTERMEDIATE,
                aliases = listOf("fpsc", "fp sc")
            )
        )

        put(
            Stitch.BackPostSc,
            StitchInfo(
                stitch = Stitch.BackPostSc,
                category = StitchCategory.POST,
                description = "Single crochet worked around the post of a stitch from the back.",
                instructions = "Insert hook from back to front to back around the post of the indicated stitch, yarn over, pull up a loop, yarn over, pull through both loops.",
                difficulty = StitchDifficulty.INTERMEDIATE,
                aliases = listOf("bpsc", "bp sc")
            )
        )

        put(
            Stitch.FrontPostHdc,
            StitchInfo(
                stitch = Stitch.FrontPostHdc,
                category = StitchCategory.POST,
                description = "Half double crochet worked around the post from the front.",
                instructions = "Yarn over, insert hook from front to back to front around the post of the indicated stitch, yarn over, pull up a loop, yarn over, pull through all 3 loops.",
                difficulty = StitchDifficulty.INTERMEDIATE,
                aliases = listOf("fphdc", "fp hdc")
            )
        )

        put(
            Stitch.BackPostHdc,
            StitchInfo(
                stitch = Stitch.BackPostHdc,
                category = StitchCategory.POST,
                description = "Half double crochet worked around the post from the back.",
                instructions = "Yarn over, insert hook from back to front to back around the post of the indicated stitch, yarn over, pull up a loop, yarn over, pull through all 3 loops.",
                difficulty = StitchDifficulty.INTERMEDIATE,
                aliases = listOf("bphdc", "bp hdc")
            )
        )

        put(
            Stitch.FrontPostDc,
            StitchInfo(
                stitch = Stitch.FrontPostDc,
                category = StitchCategory.POST,
                description = "Double crochet worked around the post from the front. Creates raised ridges and cables.",
                instructions = "Yarn over, insert hook from front to back to front around the post of the indicated stitch, yarn over, pull up a loop, [yarn over, pull through 2 loops] twice.",
                difficulty = StitchDifficulty.INTERMEDIATE,
                aliases = listOf("fpdc", "fp dc")
            )
        )

        put(
            Stitch.BackPostDc,
            StitchInfo(
                stitch = Stitch.BackPostDc,
                category = StitchCategory.POST,
                description = "Double crochet worked around the post from the back. Used with FPDC to create ribbing.",
                instructions = "Yarn over, insert hook from back to front to back around the post of the indicated stitch, yarn over, pull up a loop, [yarn over, pull through 2 loops] twice.",
                difficulty = StitchDifficulty.INTERMEDIATE,
                aliases = listOf("bpdc", "bp dc")
            )
        )

        put(
            Stitch.FrontPostTr,
            StitchInfo(
                stitch = Stitch.FrontPostTr,
                category = StitchCategory.POST,
                description = "Treble crochet worked around the post from the front. Creates prominent raised texture.",
                instructions = "Yarn over twice, insert hook from front to back to front around the post of the indicated stitch, yarn over, pull up a loop, [yarn over, pull through 2 loops] 3 times.",
                difficulty = StitchDifficulty.ADVANCED,
                aliases = listOf("fptr", "fp tr")
            )
        )

        put(
            Stitch.BackPostTr,
            StitchInfo(
                stitch = Stitch.BackPostTr,
                category = StitchCategory.POST,
                description = "Treble crochet worked around the post from the back.",
                instructions = "Yarn over twice, insert hook from back to front to back around the post of the indicated stitch, yarn over, pull up a loop, [yarn over, pull through 2 loops] 3 times.",
                difficulty = StitchDifficulty.ADVANCED,
                aliases = listOf("bptr", "bp tr")
            )
        )

        // ==================== EXTENDED STITCHES ====================

        put(
            Stitch.ExtendedSc,
            StitchInfo(
                stitch = Stitch.ExtendedSc,
                category = StitchCategory.EXTENDED,
                description = "A slightly taller single crochet with an extra chain in the middle. Creates a more elastic fabric.",
                instructions = "Insert hook in stitch, yarn over, pull up a loop, yarn over, pull through 1 loop (chain made), yarn over, pull through both loops.",
                difficulty = StitchDifficulty.BEGINNER,
                aliases = listOf("esc", "exsc")
            )
        )

        put(
            Stitch.ExtendedHdc,
            StitchInfo(
                stitch = Stitch.ExtendedHdc,
                category = StitchCategory.EXTENDED,
                description = "A taller half double crochet with an extra chain.",
                instructions = "Yarn over, insert hook in stitch, yarn over, pull up a loop, yarn over, pull through 1 loop (chain made), yarn over, pull through all 3 loops.",
                difficulty = StitchDifficulty.BEGINNER,
                aliases = listOf("ehdc", "exhdc")
            )
        )

        put(
            Stitch.ExtendedDc,
            StitchInfo(
                stitch = Stitch.ExtendedDc,
                category = StitchCategory.EXTENDED,
                description = "A taller double crochet with an extra chain.",
                instructions = "Yarn over, insert hook in stitch, yarn over, pull up a loop, yarn over, pull through 1 loop (chain made), [yarn over, pull through 2 loops] twice.",
                difficulty = StitchDifficulty.INTERMEDIATE,
                aliases = listOf("edc", "exdc")
            )
        )

        // ==================== OTHER ====================

        put(
            Stitch.Skip,
            StitchInfo(
                stitch = Stitch.Skip,
                category = StitchCategory.OTHER,
                description = "Skip the next stitch without working into it. Used to create spaces or shaping.",
                instructions = "Do not work into the next stitch. Continue to the following stitch.",
                difficulty = StitchDifficulty.BEGINNER,
                aliases = listOf("sk", "skip")
            )
        )
    }

    /**
     * Get all stitch info entries.
     */
    val allStitches: List<StitchInfo> = stitchInfoMap.values.toList()

    /**
     * Get stitch info for a specific stitch.
     */
    fun getInfo(stitch: Stitch): StitchInfo? = stitchInfoMap[stitch]

    /**
     * Get all stitches in a category.
     */
    fun getByCategory(category: StitchCategory): List<StitchInfo> =
        allStitches.filter { it.category == category }

    /**
     * Get all stitches by difficulty level.
     */
    fun getByDifficulty(difficulty: StitchDifficulty): List<StitchInfo> =
        allStitches.filter { it.difficulty == difficulty }

    /**
     * Get stitches grouped by category.
     */
    fun groupedByCategory(): Map<StitchCategory, List<StitchInfo>> =
        allStitches.groupBy { it.category }

    /**
     * Search stitches by name, abbreviation, or alias.
     */
    fun search(query: String): List<StitchInfo> {
        val lowerQuery = query.lowercase().trim()
        if (lowerQuery.isEmpty()) return allStitches

        return allStitches.filter { info ->
            info.stitch.displayName.lowercase().contains(lowerQuery) ||
                    info.stitch.abbreviation.lowercase().contains(lowerQuery) ||
                    info.aliases.any { it.lowercase().contains(lowerQuery) } ||
                    info.description.lowercase().contains(lowerQuery)
        }
    }

    /**
     * Get info for stitches used in a pattern row.
     * Useful for contextual help.
     */
    fun getInfoForStitches(stitches: List<Stitch>): List<StitchInfo> =
        stitches.distinct().mapNotNull { getInfo(it) }
}
