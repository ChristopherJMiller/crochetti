package xyz.chrismiller.crochetti.domain.parser

import xyz.chrismiller.crochetti.domain.model.Stitch
import xyz.chrismiller.crochetti.domain.model.StitchGroup

/**
 * Parser for crochet pattern DSL.
 *
 * Grammar:
 *   pattern     := stitch_list
 *   stitch_list := stitch_item (',' stitch_item)*
 *   stitch_item := group | simple_stitch
 *   group       := '(' stitch_list ')' repetition?
 *   simple_stitch := STITCH_NAME count? repetition?
 *   count       := NUMBER
 *   repetition  := 'x' NUMBER
 *
 * Examples:
 *   - "sc 6" -> 6 single crochets
 *   - "sc 2, inc" -> 2 single crochets, then 1 increase
 *   - "(sc 2, inc) x6" -> group repeated 6 times
 *   - "sc 6, (sc 2, inc) x6" -> 6 sc, then group repeated 6 times
 */
class PatternDslParser(
    private val customStitches: Map<String, Stitch.Custom> = emptyMap()
) {
    sealed class ParseResult {
        data class Success(val groups: List<StitchGroup>) : ParseResult()
        data class Error(val message: String, val position: Int) : ParseResult()
    }

    fun parse(input: String): ParseResult {
        if (input.isBlank()) {
            return ParseResult.Success(emptyList())
        }

        return try {
            val lexer = Lexer(input)
            val tokens = lexer.tokenize()
            val parser = Parser(tokens, customStitches)
            ParseResult.Success(parser.parse())
        } catch (e: ParseException) {
            ParseResult.Error(e.message ?: "Parse error", e.position)
        }
    }

    // Token types
    private sealed class Token {
        data class StitchName(val name: String, val pos: Int) : Token()
        data class Number(val value: Int, val pos: Int) : Token()
        data class LeftParen(val pos: Int) : Token()
        data class RightParen(val pos: Int) : Token()
        data class Comma(val pos: Int) : Token()
        data class Times(val pos: Int) : Token()
        data class Eof(val pos: Int) : Token()

        val position: Int
            get() = when (this) {
                is StitchName -> pos
                is Number -> pos
                is LeftParen -> pos
                is RightParen -> pos
                is Comma -> pos
                is Times -> pos
                is Eof -> pos
            }
    }

    private class ParseException(message: String, val position: Int) : Exception(message)

    // Lexer
    private class Lexer(private val input: String) {
        private var pos = 0

        fun tokenize(): List<Token> {
            val tokens = mutableListOf<Token>()

            while (pos < input.length) {
                skipWhitespace()
                if (pos >= input.length) break

                val c = input[pos]
                when {
                    c == '(' -> {
                        tokens.add(Token.LeftParen(pos))
                        pos++
                    }
                    c == ')' -> {
                        tokens.add(Token.RightParen(pos))
                        pos++
                    }
                    c == ',' -> {
                        tokens.add(Token.Comma(pos))
                        pos++
                    }
                    c == 'x' || c == 'X' -> {
                        // Check if this is the 'x' multiplier or part of a stitch name
                        val nextChar = input.getOrNull(pos + 1)
                        if (nextChar != null && (nextChar.isDigit() || nextChar.isWhitespace())) {
                            tokens.add(Token.Times(pos))
                            pos++
                        } else {
                            tokens.add(readStitchName())
                        }
                    }
                    c.isDigit() -> tokens.add(readNumber())
                    c.isLetter() -> tokens.add(readStitchName())
                    else -> throw ParseException("Unexpected character: '$c'", pos)
                }
            }

            tokens.add(Token.Eof(pos))
            return tokens
        }

        private fun skipWhitespace() {
            while (pos < input.length && input[pos].isWhitespace()) {
                pos++
            }
        }

        private fun readNumber(): Token.Number {
            val startPos = pos
            val sb = StringBuilder()
            while (pos < input.length && input[pos].isDigit()) {
                sb.append(input[pos])
                pos++
            }
            return Token.Number(sb.toString().toInt(), startPos)
        }

        private fun readStitchName(): Token.StitchName {
            val startPos = pos
            val sb = StringBuilder()

            // Read letters, digits, and spaces that form stitch names
            while (pos < input.length) {
                val c = input[pos]
                when {
                    c.isLetter() -> {
                        sb.append(c)
                        pos++
                    }
                    c.isDigit() -> {
                        // Check if this digit is part of a stitch name like "sc2tog", "dc2tog"
                        // A digit is part of the stitch name if followed by letters (like "2tog")
                        val remaining = input.substring(pos)
                        val digitAndLetters = remaining.takeWhile { it.isDigit() || it.isLetter() }
                        if (digitAndLetters.length > 1 && digitAndLetters.drop(1).any { it.isLetter() }) {
                            // This is part of a stitch name like "2tog"
                            sb.append(digitAndLetters)
                            pos += digitAndLetters.length
                        } else {
                            // This is a count, not part of stitch name
                            break
                        }
                    }
                    c == ' ' -> {
                        // Check if this is a compound stitch name like "sl st", "sc inc", "fp dc", "bp hdc"
                        val remaining = input.substring(pos).trimStart()
                        val nextWord = remaining.takeWhile { it.isLetter() }.lowercase()
                        // Allow compound names: "sl st", "sc inc/dec", "fp/bp sc/hdc/dc/tr"
                        if (nextWord in listOf("st", "inc", "dec", "tog", "sc", "hdc", "dc", "tr")) {
                            sb.append(' ')
                            pos++
                            // Skip additional whitespace
                            while (pos < input.length && input[pos] == ' ') pos++
                        } else {
                            break
                        }
                    }
                    else -> break
                }
            }

            return Token.StitchName(sb.toString().trim().lowercase(), startPos)
        }
    }

    // Parser
    private class Parser(
        private val tokens: List<Token>,
        private val customStitches: Map<String, Stitch.Custom>
    ) {
        private var current = 0

        fun parse(): List<StitchGroup> {
            val groups = mutableListOf<StitchGroup>()

            // Handle empty input
            if (peek() is Token.Eof) {
                return emptyList()
            }

            groups.add(parseStitchItem())

            while (peek() is Token.Comma) {
                advance() // consume comma
                groups.add(parseStitchItem())
            }

            expect<Token.Eof>("end of input")
            return groups
        }

        private fun parseStitchItem(): StitchGroup {
            return when (peek()) {
                is Token.LeftParen -> parseGroup()
                is Token.StitchName -> parseSimpleStitch()
                else -> throw ParseException(
                    "Expected stitch name or '('",
                    peek().position
                )
            }
        }

        private fun parseGroup(): StitchGroup {
            advance() // consume '('

            val innerItems = mutableListOf<StitchGroup>()
            innerItems.add(parseStitchItem())

            while (peek() is Token.Comma) {
                advance() // consume comma
                innerItems.add(parseStitchItem())
            }

            expect<Token.RightParen>("')'")

            val repetitions = parseOptionalRepetition()

            // Flatten inner groups into a single StitchGroup
            val allStitches = innerItems.flatMap { group ->
                (1..group.repetitions).flatMap { group.stitches }
            }

            return StitchGroup(allStitches, repetitions)
        }

        private fun parseSimpleStitch(): StitchGroup {
            val nameToken = expect<Token.StitchName>("stitch name")
            val stitch = resolveStitch(nameToken.name, nameToken.pos)

            // Optional count
            val count = if (peek() is Token.Number) {
                (advance() as Token.Number).value
            } else {
                1
            }

            // Optional repetition
            val repetitions = parseOptionalRepetition()

            return StitchGroup(
                stitches = List(count) { stitch },
                repetitions = repetitions
            )
        }

        private fun parseOptionalRepetition(): Int {
            return if (peek() is Token.Times) {
                advance() // consume 'x'
                val numToken = expect<Token.Number>("number after 'x'")
                numToken.value
            } else {
                1
            }
        }

        private fun resolveStitch(name: String, position: Int): Stitch {
            // Try built-in stitch first
            Stitch.fromAbbreviation(name)?.let { return it }

            // Try custom stitch
            customStitches[name]?.let { return it }

            throw ParseException("Unknown stitch: '$name'", position)
        }

        private fun peek(): Token = tokens.getOrElse(current) { tokens.last() }

        private fun advance(): Token {
            val token = peek()
            if (token !is Token.Eof) current++
            return token
        }

        private inline fun <reified T : Token> expect(expected: String): T {
            val token = peek()
            if (token is T) {
                advance()
                return token
            }
            throw ParseException("Expected $expected", token.position)
        }
    }

    companion object {
        /**
         * Convenience function to parse a pattern string.
         */
        fun parsePattern(input: String): ParseResult {
            return PatternDslParser().parse(input)
        }

        /**
         * Parse and return groups or null on error.
         */
        fun parseOrNull(input: String): List<StitchGroup>? {
            return when (val result = parsePattern(input)) {
                is ParseResult.Success -> result.groups
                is ParseResult.Error -> null
            }
        }
    }
}
