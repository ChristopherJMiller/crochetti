package xyz.chrismiller.crochetti.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single crochet stitch type.
 * Each stitch has an associated count (how many stitches it produces/consumes).
 */
@Serializable
sealed class Stitch {
    abstract val stitchCount: Int
    abstract val abbreviation: String
    abstract val displayName: String

    // ==================== BASIC STITCHES ====================

    @Serializable
    @SerialName("ch")
    data object Chain : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "ch"
        override val displayName: String = "Chain"
    }

    @Serializable
    @SerialName("sl")
    data object SlipStitch : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "sl st"
        override val displayName: String = "Slip Stitch"
    }

    @Serializable
    @SerialName("sc")
    data object SingleCrochet : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "sc"
        override val displayName: String = "Single Crochet"
    }

    @Serializable
    @SerialName("hdc")
    data object HalfDoubleCrochet : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "hdc"
        override val displayName: String = "Half Double Crochet"
    }

    @Serializable
    @SerialName("dc")
    data object DoubleCrochet : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "dc"
        override val displayName: String = "Double Crochet"
    }

    @Serializable
    @SerialName("tr")
    data object Treble : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "tr"
        override val displayName: String = "Treble Crochet"
    }

    @Serializable
    @SerialName("dtr")
    data object DoubleTreble : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "dtr"
        override val displayName: String = "Double Treble Crochet"
    }

    @Serializable
    @SerialName("trtr")
    data object TripleTreble : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "trtr"
        override val displayName: String = "Triple Treble Crochet"
    }

    // ==================== SHAPING STITCHES ====================

    @Serializable
    @SerialName("inc")
    data object Increase : Stitch() {
        override val stitchCount: Int = 2
        override val abbreviation: String = "inc"
        override val displayName: String = "Increase"
    }

    @Serializable
    @SerialName("dec")
    data object Decrease : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "dec"
        override val displayName: String = "Decrease"
    }

    @Serializable
    @SerialName("sc2tog")
    data object Sc2Tog : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "sc2tog"
        override val displayName: String = "Single Crochet 2 Together"
    }

    @Serializable
    @SerialName("hdc2tog")
    data object Hdc2Tog : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "hdc2tog"
        override val displayName: String = "Half Double Crochet 2 Together"
    }

    @Serializable
    @SerialName("dc2tog")
    data object Dc2Tog : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "dc2tog"
        override val displayName: String = "Double Crochet 2 Together"
    }

    @Serializable
    @SerialName("tr2tog")
    data object Tr2Tog : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "tr2tog"
        override val displayName: String = "Treble Crochet 2 Together"
    }

    // ==================== TEXTURED STITCHES ====================

    @Serializable
    @SerialName("bo")
    data object Bobble : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "bo"
        override val displayName: String = "Bobble"
    }

    @Serializable
    @SerialName("pc")
    data object Popcorn : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "pc"
        override val displayName: String = "Popcorn"
    }

    @Serializable
    @SerialName("ps")
    data object PuffStitch : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "ps"
        override val displayName: String = "Puff Stitch"
    }

    @Serializable
    @SerialName("cl")
    data object Cluster : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "cl"
        override val displayName: String = "Cluster"
    }

    @Serializable
    @SerialName("sh")
    data object Shell : Stitch() {
        override val stitchCount: Int = 5 // Typically 5 DC in same stitch
        override val abbreviation: String = "sh"
        override val displayName: String = "Shell"
    }

    // ==================== POST STITCHES ====================

    @Serializable
    @SerialName("fpsc")
    data object FrontPostSc : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "fpsc"
        override val displayName: String = "Front Post Single Crochet"
    }

    @Serializable
    @SerialName("bpsc")
    data object BackPostSc : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "bpsc"
        override val displayName: String = "Back Post Single Crochet"
    }

    @Serializable
    @SerialName("fphdc")
    data object FrontPostHdc : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "fphdc"
        override val displayName: String = "Front Post Half Double Crochet"
    }

    @Serializable
    @SerialName("bphdc")
    data object BackPostHdc : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "bphdc"
        override val displayName: String = "Back Post Half Double Crochet"
    }

    @Serializable
    @SerialName("fpdc")
    data object FrontPostDc : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "fpdc"
        override val displayName: String = "Front Post Double Crochet"
    }

    @Serializable
    @SerialName("bpdc")
    data object BackPostDc : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "bpdc"
        override val displayName: String = "Back Post Double Crochet"
    }

    @Serializable
    @SerialName("fptr")
    data object FrontPostTr : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "fptr"
        override val displayName: String = "Front Post Treble Crochet"
    }

    @Serializable
    @SerialName("bptr")
    data object BackPostTr : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "bptr"
        override val displayName: String = "Back Post Treble Crochet"
    }

    // ==================== EXTENDED STITCHES ====================

    @Serializable
    @SerialName("esc")
    data object ExtendedSc : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "esc"
        override val displayName: String = "Extended Single Crochet"
    }

    @Serializable
    @SerialName("ehdc")
    data object ExtendedHdc : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "ehdc"
        override val displayName: String = "Extended Half Double Crochet"
    }

    @Serializable
    @SerialName("edc")
    data object ExtendedDc : Stitch() {
        override val stitchCount: Int = 1
        override val abbreviation: String = "edc"
        override val displayName: String = "Extended Double Crochet"
    }

    // ==================== OTHER ====================

    @Serializable
    @SerialName("sk")
    data object Skip : Stitch() {
        override val stitchCount: Int = 0
        override val abbreviation: String = "sk"
        override val displayName: String = "Skip"
    }

    @Serializable
    @SerialName("custom")
    data class Custom(
        val name: String,
        override val stitchCount: Int = 1
    ) : Stitch() {
        override val abbreviation: String = name
        override val displayName: String = name
    }

    companion object {
        val builtInStitches: List<Stitch> = listOf(
            // Basic
            Chain,
            SlipStitch,
            SingleCrochet,
            HalfDoubleCrochet,
            DoubleCrochet,
            Treble,
            DoubleTreble,
            TripleTreble,
            // Shaping
            Increase,
            Decrease,
            Sc2Tog,
            Hdc2Tog,
            Dc2Tog,
            Tr2Tog,
            // Textured
            Bobble,
            Popcorn,
            PuffStitch,
            Cluster,
            Shell,
            // Post
            FrontPostSc,
            BackPostSc,
            FrontPostHdc,
            BackPostHdc,
            FrontPostDc,
            BackPostDc,
            FrontPostTr,
            BackPostTr,
            // Extended
            ExtendedSc,
            ExtendedHdc,
            ExtendedDc,
            // Other
            Skip
        )

        fun fromAbbreviation(abbr: String): Stitch? {
            return when (abbr.lowercase().trim()) {
                // Basic stitches
                "ch" -> Chain
                "sl", "sl st", "ss", "slst" -> SlipStitch
                "sc" -> SingleCrochet
                "hdc" -> HalfDoubleCrochet
                "dc" -> DoubleCrochet
                "tr", "tc" -> Treble
                "dtr" -> DoubleTreble
                "trtr", "ttr" -> TripleTreble

                // Shaping
                "inc", "sc inc" -> Increase
                "dec", "sc dec" -> Decrease
                "sc2tog" -> Sc2Tog
                "hdc2tog" -> Hdc2Tog
                "dc2tog" -> Dc2Tog
                "tr2tog" -> Tr2Tog

                // Textured
                "bo", "bob", "bobble" -> Bobble
                "pc", "popcorn" -> Popcorn
                "ps", "puff" -> PuffStitch
                "cl", "cluster" -> Cluster
                "sh", "shell" -> Shell

                // Post stitches
                "fpsc", "fp sc" -> FrontPostSc
                "bpsc", "bp sc" -> BackPostSc
                "fphdc", "fp hdc" -> FrontPostHdc
                "bphdc", "bp hdc" -> BackPostHdc
                "fpdc", "fp dc" -> FrontPostDc
                "bpdc", "bp dc" -> BackPostDc
                "fptr", "fp tr" -> FrontPostTr
                "bptr", "bp tr" -> BackPostTr

                // Extended
                "esc", "exsc" -> ExtendedSc
                "ehdc", "exhdc" -> ExtendedHdc
                "edc", "exdc" -> ExtendedDc

                // Other
                "sk", "skip" -> Skip

                else -> null
            }
        }
    }
}
