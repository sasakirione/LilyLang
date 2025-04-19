import com.sasakirione.Keywords


/**
 * Token types for the LilyLang language
 */
sealed class TokenType {
    // Keywords
    object VAR : TokenType()
    object PRINT : TokenType()
    object LIST : TokenType()

    // Operators
    object PLUS : TokenType()
    object MINUS : TokenType()
    object MUL : TokenType()
    object DIV : TokenType()
    object MOD : TokenType()
    object EQUALS : TokenType()

    // Logical operators
    object AND : TokenType()
    object OR : TokenType()
    object NOT : TokenType()

    // Literals
    object INT_LITERAL : TokenType()
    object BOOLEAN_LITERAL : TokenType()
    object STRING_LITERAL : TokenType()

    // Identifiers
    object IDENTIFIER : TokenType()

    // End of file
    object EOF : TokenType()
}

/**
 * Represents a token in the source code
 *
 * @property type The type of the token
 * @property value The string value of the token
 * @property line The line number where the token appears
 * @property column The column number where the token starts
 */
data class Token(
    val type: TokenType,
    val value: String,
    val line: Int,
    val column: Int
)

/**
 * Lexical analyser (tokeniser) for LilyLang
 * Converts source code into a stream of tokens
 */
class Lexer(private val source: String, private val errorReporter: ErrorReporter? = null) {
    private var position = 0
    private var line = 1
    private var column = 1
    private val tokens = mutableListOf<Token>()

    /**
     * Tokenise the entire source code
     *
     * @return List of tokens
     */
    fun tokenize(): List<Token> {
        while (position < source.length) {
            val char = source[position]

            when {
                char.isWhitespace() -> skipWhitespace()
                char.isDigit() -> tokenizeNumber()
                char.isLetter() -> tokenizeIdentifier()
                char == '"' -> tokenizeString()
                char == Keywords.PLUS -> addToken(TokenType.PLUS, "+")
                char == Keywords.MINUS -> addToken(TokenType.MINUS, "-")
                char == Keywords.MUL -> addToken(TokenType.MUL, "*")
                char == Keywords.DIV -> addToken(TokenType.DIV, "/")
                char == Keywords.MOD -> addToken(TokenType.MOD, "%")
                char == Keywords.EQUALS -> addToken(TokenType.EQUALS, "=")
                else -> {
                    // Report unknown character as a lexical error
                    errorReporter?.reportLexicalError("Unexpected character: '$char'", line, column)
                    advance()
                }
            }
        }

        // Add EOF token
        tokens.add(Token(TokenType.EOF, "", line, column))
        return tokens
    }

    private fun skipWhitespace() {
        while (position < source.length && source[position].isWhitespace()) {
            if (source[position] == '\n') {
                line++
                column = 1
            } else {
                column++
            }
            position++
        }
    }

    private fun tokenizeNumber() {
        val start = position
        val startColumn = column

        while (position < source.length && source[position].isDigit()) {
            advance()
        }

        val value = source.substring(start, position)
        tokens.add(Token(TokenType.INT_LITERAL, value, line, startColumn))
    }

    private fun tokenizeIdentifier() {
        val start = position
        val startColumn = column

        while (position < source.length && (source[position].isLetterOrDigit() || source[position] == '_')) {
            advance()
        }

        val value = source.substring(start, position)
        val type = when (value) {
            Keywords.VAR -> TokenType.VAR
            Keywords.PRINT -> TokenType.PRINT
            Keywords.LIST -> TokenType.LIST
            Keywords.TRUE -> TokenType.BOOLEAN_LITERAL
            Keywords.FALSE -> TokenType.BOOLEAN_LITERAL
            Keywords.AND -> TokenType.AND
            Keywords.OR -> TokenType.OR
            Keywords.NOT -> TokenType.NOT
            else -> TokenType.IDENTIFIER
        }

        tokens.add(Token(type, value, line, startColumn))
    }

    private fun tokenizeString() {
        val start = position
        val startColumn = column

        // Skip the opening quote
        advance()

        // Read until closing quote or end of file
        while (position < source.length && source[position] != '"') {
            // Handle escaped characters if needed
            if (source[position] == '\\' && position + 1 < source.length) {
                advance() // Skip the backslash
            }

            // Handle newlines in strings
            if (source[position] == '\n') {
                line++
                column = 1
            }

            advance()
        }

        if (position >= source.length) {
            // Unterminated string
            errorReporter?.reportLexicalError("Unterminated string", line, startColumn)
            // We'll still create a token with what we have
        } else {
            // Skip the closing quote
            advance()
        }

        // Extract the string value (excluding the quotes)
        val value = source.substring(start + 1, if (position > start + 1 && source[position - 1] == '"') position - 1 else position)
        tokens.add(Token(TokenType.STRING_LITERAL, value, line, startColumn))
    }

    private fun addToken(type: TokenType, value: String) {
        tokens.add(Token(type, value, line, column))
        advance()
    }

    private fun advance() {
        position++
        column++
    }
}
