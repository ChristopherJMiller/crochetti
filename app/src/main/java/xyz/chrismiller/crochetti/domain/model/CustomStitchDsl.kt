package xyz.chrismiller.crochetti.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents where to insert the hook in a custom stitch definition.
 */
@Serializable
sealed class InsertTarget {
    /** Insert into the current working stitch */
    @Serializable
    @SerialName("st")
    data object CurrentStitch : InsertTarget()

    /** Insert into the same stitch (for cluster-type stitches) */
    @Serializable
    @SerialName("same")
    data object SameStitch : InsertTarget()

    /** Insert into a stitch at a specific offset (negative = previous stitches) */
    @Serializable
    @SerialName("offset")
    data class Offset(val offset: Int) : InsertTarget()
}

/**
 * Specifies how many loops to pull through.
 */
@Serializable
sealed class PullCount {
    /** Pull through 1 loop (default) */
    @Serializable
    @SerialName("one")
    data object One : PullCount()

    /** Pull through a specific number of loops */
    @Serializable
    @SerialName("n")
    data class Specific(val count: Int) : PullCount()

    /** Pull through all loops currently on hook */
    @Serializable
    @SerialName("all")
    data object All : PullCount()
}

/**
 * A single primitive instruction in a custom stitch definition.
 *
 * These instructions represent the fundamental actions of crochet:
 * - yo: Yarn over - wrap yarn around hook
 * - insert: Insert hook into specified location
 * - pull: Pull yarn through loops on hook
 * - repeat: Repeat a sequence of instructions
 * - chain: Shorthand for a chain stitch (yo + pull through 1)
 */
@Serializable
sealed class StitchInstruction {
    /** Yarn over - wraps yarn around hook, adds 1 loop */
    @Serializable
    @SerialName("yo")
    data object YarnOver : StitchInstruction()

    /** Insert hook into target stitch */
    @Serializable
    @SerialName("insert")
    data class Insert(val target: InsertTarget = InsertTarget.CurrentStitch) : StitchInstruction()

    /** Pull yarn through to create/close loops */
    @Serializable
    @SerialName("pull")
    data class Pull(val count: PullCount = PullCount.One) : StitchInstruction()

    /** Repeat a block of instructions n times */
    @Serializable
    @SerialName("repeat")
    data class Repeat(
        val times: Int,
        val instructions: List<StitchInstruction>
    ) : StitchInstruction()

    /** Reference to another custom stitch (for composition) */
    @Serializable
    @SerialName("ref")
    data class StitchReference(val stitchAbbreviation: String) : StitchInstruction()

    /** Chain stitch - shorthand for yo + pull through 1 */
    @Serializable
    @SerialName("chain")
    data class Chain(val count: Int = 1) : StitchInstruction()

    /**
     * Convert instruction to a human-readable string for display.
     */
    fun toDisplayString(): String = when (this) {
        is YarnOver -> "yo"
        is Insert -> when (target) {
            is InsertTarget.CurrentStitch -> "insert(st)"
            is InsertTarget.SameStitch -> "insert(same)"
            is InsertTarget.Offset -> "insert(${target.offset})"
        }
        is Pull -> when (count) {
            is PullCount.One -> "pull"
            is PullCount.All -> "pull(all)"
            is PullCount.Specific -> "pull(${count.count})"
        }
        is Repeat -> "repeat ${times} { ... }"
        is StitchReference -> stitchAbbreviation
        is Chain -> if (count == 1) "chain" else "chain $count"
    }
}

/**
 * Represents the state after executing an instruction.
 * Used for visualizing loop count progression in the editor.
 */
data class InstructionState(
    val instruction: StitchInstruction,
    val loopsAfter: Int,
    val depth: Int = 0  // For indenting repeat blocks
)

/**
 * A complete custom stitch definition.
 *
 * Custom stitches are pattern-scoped and can be used in row DSL once defined.
 *
 * Example DSL:
 * ```
 * define pbo "Partial Bobble" {
 *   yo
 *   insert(st)
 *   yo
 *   pull
 *   yo
 *   pull(2)
 *   repeat 2 {
 *     yo
 *     insert(same)
 *     yo
 *     pull
 *     yo
 *     pull(2)
 *   }
 *   yo
 *   pull(all)
 * }
 * ```
 */
@Serializable
data class CustomStitchDefinition(
    /** The abbreviation used in row DSL (e.g., "pbo") */
    val abbreviation: String,
    /** Human-readable name (e.g., "Partial Bobble") */
    val displayName: String,
    /** Optional description of what the stitch does */
    val description: String = "",
    /** The sequence of primitive instructions */
    val instructions: List<StitchInstruction>,
    /** Cached stitch count, calculated from instructions */
    val stitchCount: Int = 1,
    /** Original DSL text for re-editing */
    val rawDsl: String = ""
) {
    /**
     * Convert to a Stitch.Custom for use in pattern rows.
     */
    fun toStitch(): Stitch.Custom = Stitch.Custom(
        name = abbreviation,
        stitchCount = stitchCount
    )

    /**
     * Get the abbreviation in lowercase for consistent lookup.
     */
    val normalizedAbbreviation: String
        get() = abbreviation.lowercase().trim()
}

/**
 * Convert a list of stitch instructions to human-readable numbered steps
 * with loop count checkpoints.
 */
fun List<StitchInstruction>.toReadableSteps(): String {
    val result = mutableListOf<String>()
    var loopsOnHook = 1  // Start with 1 loop on hook
    processInstructionsReadable(this, result, loopsOnHook = loopsOnHook, indent = "")
    return result.joinToString("\n")
}

/**
 * Process instructions recursively, tracking loop count and building readable output.
 * Returns the final loop count after processing all instructions.
 */
private fun processInstructionsReadable(
    instructions: List<StitchInstruction>,
    result: MutableList<String>,
    loopsOnHook: Int,
    indent: String,
    numberOffset: Int = 0
): Int {
    var loops = loopsOnHook

    instructions.forEachIndexed { index, instruction ->
        val stepNum = index + 1 + numberOffset

        when (instruction) {
            is StitchInstruction.YarnOver -> {
                loops += 1
                result.add("$indent$stepNum. Yarn over → $loops loop${if (loops != 1) "s" else ""}")
            }
            is StitchInstruction.Insert -> {
                val target = when (instruction.target) {
                    is InsertTarget.CurrentStitch -> "stitch"
                    is InsertTarget.SameStitch -> "same stitch"
                    is InsertTarget.Offset -> "${(instruction.target as InsertTarget.Offset).offset} stitch(es) back"
                }
                result.add("$indent$stepNum. Insert hook into $target")
            }
            is StitchInstruction.Pull -> {
                val (desc, newLoops) = when (instruction.count) {
                    is PullCount.One -> {
                        // Pull through 1 keeps loop count same (pulls up a new loop)
                        "Pull through" to loops
                    }
                    is PullCount.All -> {
                        // Pull through all reduces to 1 loop
                        "Pull through all $loops loops" to 1
                    }
                    is PullCount.Specific -> {
                        val count = (instruction.count as PullCount.Specific).count
                        val remaining = maxOf(1, loops - count + 1)
                        "Pull through $count loops" to remaining
                    }
                }
                loops = newLoops
                result.add("$indent$stepNum. $desc → $loops loop${if (loops != 1) "s" else ""}")
            }
            is StitchInstruction.Repeat -> {
                result.add("$indent$stepNum. Repeat ${instruction.times} times:")
                repeat(instruction.times) { iteration ->
                    if (instruction.times > 1) {
                        result.add("$indent   [Rep ${iteration + 1}]")
                    }
                    loops = processInstructionsReadable(
                        instruction.instructions,
                        result,
                        loopsOnHook = loops,
                        indent = "$indent   "
                    )
                }
            }
            is StitchInstruction.Chain -> {
                val desc = if (instruction.count == 1) "Chain 1" else "Chain ${instruction.count}"
                result.add("$indent$stepNum. $desc")
            }
            is StitchInstruction.StitchReference -> {
                result.add("$indent$stepNum. Work ${instruction.stitchAbbreviation}")
            }
        }
    }

    return loops
}
