package xyz.chrismiller.crochetti.domain.parser

import xyz.chrismiller.crochetti.domain.model.CustomStitchDefinition
import xyz.chrismiller.crochetti.domain.model.InsertTarget
import xyz.chrismiller.crochetti.domain.model.InstructionState
import xyz.chrismiller.crochetti.domain.model.PullCount
import xyz.chrismiller.crochetti.domain.model.StitchInstruction

/**
 * Parser for custom stitch definition DSL.
 *
 * Grammar:
 *   definition      := instruction_list
 *   instruction_list := instruction*
 *   instruction     := yo | insert | pull | repeat | chain | IDENTIFIER
 *   yo              := 'yo'
 *   insert          := 'insert' '(' target ')'
 *   target          := 'st' | 'same' | NUMBER
 *   pull            := 'pull' ('(' pull_count ')')?
 *   pull_count      := NUMBER | 'all'
 *   repeat          := 'repeat' NUMBER '{' instruction_list '}'
 *   chain           := 'chain' NUMBER?
 *
 * Example:
 * ```
 * yo
 * insert(st)
 * yo
 * pull
 * yo
 * pull(2)
 * repeat 2 {
 *   yo
 *   insert(same)
 *   yo
 *   pull
 *   yo
 *   pull(2)
 * }
 * yo
 * pull(all)
 * ```
 */
class CustomStitchDslParser(
    private val existingDefinitions: Map<String, CustomStitchDefinition> = emptyMap()
) {
    sealed class ParseResult {
        data class Success(
            val definition: CustomStitchDefinition,
            val loopStates: List<InstructionState> = emptyList()
        ) : ParseResult()
        data class Error(val message: String, val line: Int, val column: Int) : ParseResult()
    }

    /**
     * Parse instructions only (without the define header).
     * Used for editing custom stitch body.
     */
    fun parseInstructions(
        input: String,
        abbreviation: String,
        displayName: String,
        description: String = ""
    ): ParseResult {
        if (input.isBlank()) {
            return ParseResult.Error("Empty stitch definition", 1, 1)
        }

        return try {
            val lexer = Lexer(input)
            val tokens = lexer.tokenize()
            val parser = Parser(tokens, existingDefinitions)
            val instructions = parser.parseInstructionList()

            if (instructions.isEmpty()) {
                return ParseResult.Error("No instructions found", 1, 1)
            }

            val calculator = StitchCountCalculator(existingDefinitions)
            val (stitchCount, loopStates) = calculator.calculateWithStates(instructions)

            ParseResult.Success(
                definition = CustomStitchDefinition(
                    abbreviation = abbreviation,
                    displayName = displayName,
                    description = description,
                    instructions = instructions,
                    stitchCount = stitchCount,
                    rawDsl = input
                ),
                loopStates = loopStates
            )
        } catch (e: ParseException) {
            ParseResult.Error(e.message ?: "Parse error", e.line, e.column)
        }
    }

    // Token types
    private sealed class Token {
        abstract val line: Int
        abstract val column: Int

        data class Keyword(val value: String, override val line: Int, override val column: Int) : Token()
        data class Identifier(val value: String, override val line: Int, override val column: Int) : Token()
        data class Number(val value: Int, override val line: Int, override val column: Int) : Token()
        data class LeftParen(override val line: Int, override val column: Int) : Token()
        data class RightParen(override val line: Int, override val column: Int) : Token()
        data class LeftBrace(override val line: Int, override val column: Int) : Token()
        data class RightBrace(override val line: Int, override val column: Int) : Token()
        data class Eof(override val line: Int, override val column: Int) : Token()
    }

    private class ParseException(message: String, val line: Int, val column: Int) : Exception(message)

    // Lexer
    private class Lexer(private val input: String) {
        private var pos = 0
        private var line = 1
        private var column = 1

        private val keywords = setOf("yo", "insert", "pull", "repeat", "chain", "st", "same", "all")

        fun tokenize(): List<Token> {
            val tokens = mutableListOf<Token>()

            while (pos < input.length) {
                skipWhitespaceAndComments()
                if (pos >= input.length) break

                val c = input[pos]
                val startLine = line
                val startColumn = column

                when {
                    c == '(' -> {
                        tokens.add(Token.LeftParen(startLine, startColumn))
                        advance()
                    }
                    c == ')' -> {
                        tokens.add(Token.RightParen(startLine, startColumn))
                        advance()
                    }
                    c == '{' -> {
                        tokens.add(Token.LeftBrace(startLine, startColumn))
                        advance()
                    }
                    c == '}' -> {
                        tokens.add(Token.RightBrace(startLine, startColumn))
                        advance()
                    }
                    c.isDigit() -> tokens.add(readNumber(startLine, startColumn))
                    c.isLetter() || c == '_' -> tokens.add(readIdentifierOrKeyword(startLine, startColumn))
                    else -> throw ParseException("Unexpected character: '$c'", startLine, startColumn)
                }
            }

            tokens.add(Token.Eof(line, column))
            return tokens
        }

        private fun advance() {
            if (pos < input.length) {
                if (input[pos] == '\n') {
                    line++
                    column = 1
                } else {
                    column++
                }
                pos++
            }
        }

        private fun skipWhitespaceAndComments() {
            while (pos < input.length) {
                val c = input[pos]
                when {
                    c.isWhitespace() -> advance()
                    c == '/' && pos + 1 < input.length && input[pos + 1] == '/' -> {
                        // Line comment
                        while (pos < input.length && input[pos] != '\n') {
                            advance()
                        }
                    }
                    else -> break
                }
            }
        }

        private fun readNumber(startLine: Int, startColumn: Int): Token.Number {
            val sb = StringBuilder()
            while (pos < input.length && input[pos].isDigit()) {
                sb.append(input[pos])
                advance()
            }
            return Token.Number(sb.toString().toInt(), startLine, startColumn)
        }

        private fun readIdentifierOrKeyword(startLine: Int, startColumn: Int): Token {
            val sb = StringBuilder()
            while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_')) {
                sb.append(input[pos])
                advance()
            }
            val value = sb.toString().lowercase()
            return if (value in keywords) {
                Token.Keyword(value, startLine, startColumn)
            } else {
                Token.Identifier(value, startLine, startColumn)
            }
        }
    }

    // Parser
    private class Parser(
        private val tokens: List<Token>,
        private val existingDefinitions: Map<String, CustomStitchDefinition>
    ) {
        private var current = 0

        fun parseInstructionList(): List<StitchInstruction> {
            val instructions = mutableListOf<StitchInstruction>()

            while (!isAtEnd() && peek() !is Token.RightBrace) {
                instructions.add(parseInstruction())
            }

            return instructions
        }

        private fun parseInstruction(): StitchInstruction {
            return when (val token = peek()) {
                is Token.Keyword -> when (token.value) {
                    "yo" -> parseYo()
                    "insert" -> parseInsert()
                    "pull" -> parsePull()
                    "repeat" -> parseRepeat()
                    "chain" -> parseChain()
                    else -> throw ParseException("Unexpected keyword: ${token.value}", token.line, token.column)
                }
                is Token.Identifier -> parseStitchReference()
                else -> throw ParseException("Expected instruction", token.line, token.column)
            }
        }

        private fun parseYo(): StitchInstruction {
            expectKeyword("yo")
            return StitchInstruction.YarnOver
        }

        private fun parseInsert(): StitchInstruction {
            expectKeyword("insert")
            expect<Token.LeftParen>("'('")
            val target = parseInsertTarget()
            expect<Token.RightParen>("')'")
            return StitchInstruction.Insert(target)
        }

        private fun parseInsertTarget(): InsertTarget {
            return when (val token = peek()) {
                is Token.Keyword -> when (token.value) {
                    "st" -> {
                        advance()
                        InsertTarget.CurrentStitch
                    }
                    "same" -> {
                        advance()
                        InsertTarget.SameStitch
                    }
                    else -> throw ParseException("Expected 'st', 'same', or number", token.line, token.column)
                }
                is Token.Number -> {
                    advance()
                    InsertTarget.Offset(token.value)
                }
                else -> throw ParseException("Expected insert target", token.line, token.column)
            }
        }

        private fun parsePull(): StitchInstruction {
            expectKeyword("pull")

            // Check for optional (count)
            val count = if (peek() is Token.LeftParen) {
                advance() // consume '('
                val pullCount = parsePullCount()
                expect<Token.RightParen>("')'")
                pullCount
            } else {
                PullCount.One
            }

            return StitchInstruction.Pull(count)
        }

        private fun parsePullCount(): PullCount {
            return when (val token = peek()) {
                is Token.Keyword -> {
                    if (token.value == "all") {
                        advance()
                        PullCount.All
                    } else {
                        throw ParseException("Expected number or 'all'", token.line, token.column)
                    }
                }
                is Token.Number -> {
                    advance()
                    PullCount.Specific(token.value)
                }
                else -> throw ParseException("Expected number or 'all'", token.line, token.column)
            }
        }

        private fun parseRepeat(): StitchInstruction {
            expectKeyword("repeat")

            val timesToken = expect<Token.Number>("repeat count")
            val times = timesToken.value

            expect<Token.LeftBrace>("'{'")
            val instructions = parseInstructionList()
            expect<Token.RightBrace>("'}'")

            return StitchInstruction.Repeat(times, instructions)
        }

        private fun parseChain(): StitchInstruction {
            expectKeyword("chain")

            // Optional count
            val count = if (peek() is Token.Number) {
                (advance() as Token.Number).value
            } else {
                1
            }

            return StitchInstruction.Chain(count)
        }

        private fun parseStitchReference(): StitchInstruction {
            val token = expect<Token.Identifier>("stitch name")

            // Verify the referenced stitch exists
            if (token.value !in existingDefinitions) {
                throw ParseException(
                    "Unknown stitch: '${token.value}'",
                    token.line,
                    token.column
                )
            }

            return StitchInstruction.StitchReference(token.value)
        }

        private fun expectKeyword(keyword: String) {
            val token = peek()
            if (token is Token.Keyword && token.value == keyword) {
                advance()
            } else {
                throw ParseException("Expected '$keyword'", token.line, token.column)
            }
        }

        private inline fun <reified T : Token> expect(expected: String): T {
            val token = peek()
            if (token is T) {
                advance()
                return token
            }
            throw ParseException("Expected $expected", token.line, token.column)
        }

        private fun peek(): Token = tokens.getOrElse(current) { tokens.last() }

        private fun advance(): Token {
            val token = peek()
            if (token !is Token.Eof) current++
            return token
        }

        private fun isAtEnd(): Boolean = peek() is Token.Eof
    }

    companion object {
        /**
         * Parse custom stitch instructions from DSL text.
         */
        fun parse(
            input: String,
            abbreviation: String,
            displayName: String,
            description: String = "",
            existingDefinitions: Map<String, CustomStitchDefinition> = emptyMap()
        ): ParseResult {
            return CustomStitchDslParser(existingDefinitions).parseInstructions(
                input, abbreviation, displayName, description
            )
        }
    }
}

/**
 * Calculates the stitch count produced by a custom stitch definition.
 *
 * Tracks loop state through instructions to determine how many stitches
 * are produced by the definition.
 */
class StitchCountCalculator(
    private val definitions: Map<String, CustomStitchDefinition> = emptyMap()
) {
    private data class LoopState(
        val loopsOnHook: Int = 1,
        val stitchesProduced: Int = 0,
        val insertionsMade: Int = 0,
        val lastInsertWasSame: Boolean = false
    )

    /**
     * Calculate the number of stitches produced by a list of instructions.
     */
    fun calculate(instructions: List<StitchInstruction>): Int {
        val finalState = processInstructions(instructions, LoopState())
        // Most stitches produce 1 output stitch unless they're increases/decreases
        return maxOf(1, finalState.stitchesProduced)
    }

    /**
     * Calculate stitch count and return per-instruction loop states.
     * Returns a pair of (stitchCount, loopStates).
     */
    fun calculateWithStates(instructions: List<StitchInstruction>): Pair<Int, List<InstructionState>> {
        val states = mutableListOf<InstructionState>()
        val finalState = processInstructionsWithStates(instructions, LoopState(), states, depth = 0)
        val stitchCount = maxOf(1, finalState.stitchesProduced)
        return stitchCount to states
    }

    private fun processInstructionsWithStates(
        instructions: List<StitchInstruction>,
        initialState: LoopState,
        states: MutableList<InstructionState>,
        depth: Int
    ): LoopState {
        var state = initialState

        for (instruction in instructions) {
            state = when (instruction) {
                is StitchInstruction.YarnOver -> {
                    state.copy(loopsOnHook = state.loopsOnHook + 1)
                }
                is StitchInstruction.Insert -> {
                    val isSame = instruction.target is InsertTarget.SameStitch
                    val newInsertions = if (isSame) state.insertionsMade else state.insertionsMade + 1
                    state.copy(
                        insertionsMade = newInsertions,
                        lastInsertWasSame = isSame
                    )
                }
                is StitchInstruction.Pull -> {
                    when (instruction.count) {
                        is PullCount.All -> {
                            state.copy(
                                loopsOnHook = 1,
                                stitchesProduced = state.stitchesProduced + 1
                            )
                        }
                        is PullCount.Specific -> {
                            val pulled = instruction.count.count
                            val newLoops = maxOf(1, state.loopsOnHook - pulled + 1)
                            val produced = if (pulled >= state.loopsOnHook) {
                                state.stitchesProduced + 1
                            } else {
                                state.stitchesProduced
                            }
                            state.copy(loopsOnHook = newLoops, stitchesProduced = produced)
                        }
                        is PullCount.One -> {
                            state.copy(loopsOnHook = state.loopsOnHook)
                        }
                    }
                }
                is StitchInstruction.Repeat -> {
                    // For repeat, we'll show the repeat instruction itself, then process contents
                    states.add(InstructionState(instruction, state.loopsOnHook, depth))
                    var repeatState = state
                    repeat(instruction.times) { iteration ->
                        repeatState = processInstructionsWithStates(
                            instruction.instructions,
                            repeatState,
                            states,
                            depth + 1
                        )
                    }
                    repeatState
                }
                is StitchInstruction.StitchReference -> {
                    val refDef = definitions[instruction.stitchAbbreviation.lowercase()]
                    if (refDef != null) {
                        state.copy(stitchesProduced = state.stitchesProduced + refDef.stitchCount)
                    } else {
                        state
                    }
                }
                is StitchInstruction.Chain -> {
                    state.copy(stitchesProduced = state.stitchesProduced + instruction.count)
                }
            }

            // Record state after instruction (except for Repeat which was handled above)
            if (instruction !is StitchInstruction.Repeat) {
                states.add(InstructionState(instruction, state.loopsOnHook, depth))
            }
        }
        return state
    }

    private fun processInstructions(
        instructions: List<StitchInstruction>,
        initialState: LoopState
    ): LoopState {
        var state = initialState

        for (instruction in instructions) {
            state = when (instruction) {
                is StitchInstruction.YarnOver -> {
                    state.copy(loopsOnHook = state.loopsOnHook + 1)
                }
                is StitchInstruction.Insert -> {
                    val isSame = instruction.target is InsertTarget.SameStitch
                    val newInsertions = if (isSame) state.insertionsMade else state.insertionsMade + 1
                    state.copy(
                        insertionsMade = newInsertions,
                        lastInsertWasSame = isSame
                    )
                }
                is StitchInstruction.Pull -> {
                    when (instruction.count) {
                        is PullCount.All -> {
                            // Pulling through all loops closes the stitch
                            state.copy(
                                loopsOnHook = 1,
                                stitchesProduced = state.stitchesProduced + 1
                            )
                        }
                        is PullCount.Specific -> {
                            val pulled = instruction.count.count
                            val newLoops = maxOf(1, state.loopsOnHook - pulled + 1)
                            // If we pulled all remaining loops, that closes a stitch
                            val produced = if (pulled >= state.loopsOnHook) {
                                state.stitchesProduced + 1
                            } else {
                                state.stitchesProduced
                            }
                            state.copy(loopsOnHook = newLoops, stitchesProduced = produced)
                        }
                        is PullCount.One -> {
                            // Pulling through 1 just creates a new loop without reducing
                            state.copy(loopsOnHook = state.loopsOnHook)
                        }
                    }
                }
                is StitchInstruction.Repeat -> {
                    var repeatState = state
                    repeat(instruction.times) {
                        repeatState = processInstructions(instruction.instructions, repeatState)
                    }
                    repeatState
                }
                is StitchInstruction.StitchReference -> {
                    val refDef = definitions[instruction.stitchAbbreviation.lowercase()]
                    if (refDef != null) {
                        state.copy(stitchesProduced = state.stitchesProduced + refDef.stitchCount)
                    } else {
                        state
                    }
                }
                is StitchInstruction.Chain -> {
                    // Each chain produces 1 stitch
                    state.copy(stitchesProduced = state.stitchesProduced + instruction.count)
                }
            }
        }
        return state
    }
}
