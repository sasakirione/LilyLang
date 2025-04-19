import com.sasakirione.Expression
import com.sasakirione.Program
import com.sasakirione.Statement

/**
 * Parser for LilyLang
 * Converts a stream of tokens into an Abstract Syntax Tree (AST)
 */
class Parser(private val tokens: List<Token>, private val errorReporter: ErrorReporter? = null) {
    private var currentPosition = 0

    /**
     * Parse the token stream into a Program AST
     *
     * @return Program AST representing the entire program
     */
    fun parseProgram(): Program {
        val statements = mutableListOf<Statement>()

        while (currentPosition < tokens.size && !isAtEnd()) {
            statements.add(parseStatement())
        }

        return Program(statements)
    }

    /**
     * Parse a single statement
     *
     * @return Statement AST node
     */
    fun parseStatement(): Statement {
        val token = peek()

        return when (token.type) {
            TokenType.VAR -> parseVarDeclaration()
            TokenType.PRINT -> parsePrintStatement()
            TokenType.IDENTIFIER -> parseAssignment()
            else -> {
                errorReporter?.reportSyntaxError("Unexpected token: ${token.value}", token.line, token.column)
                // Return a dummy statement to allow parsing to continue
                Statement.Print(Expression.IntLiteral(0))
            }
        }
    }

    /**
     * Parse a variable declaration statement (var x = expr)
     *
     * @return VarDecl statement
     */
    private fun parseVarDeclaration(): Statement {
        // Consume 'var' token
        advance()

        // Next token should be an identifier
        val varName = consume(TokenType.IDENTIFIER, "Expected variable name after 'var'").value

        // Next token should be '='
        consume(TokenType.EQUALS, "Expected '=' after variable name")

        // Parse the expression after '='
        val expr = parseExpression()

        return Statement.VarDecl(varName, expr)
    }

    /**
     * Parse a print statement (print expr)
     *
     * @return Print statement
     */
    private fun parsePrintStatement(): Statement {
        // Consume 'print' token
        advance()

        // Parse the expression to print
        val expr = parseExpression()

        return Statement.Print(expr)
    }

    /**
     * Parse a variable assignment statement (x = expr)
     *
     * @return VarAssign statement
     */
    private fun parseAssignment(): Statement {
        // Get the variable name
        val varName = consume(TokenType.IDENTIFIER, "Expected variable name").value

        // Next token should be '='
        consume(TokenType.EQUALS, "Expected '=' after variable name")

        // Parse the expression after '='
        val expr = parseExpression()

        return Statement.VarAssign(varName, expr)
    }

    /**
     * Parse an expression
     *
     * @return Expression AST node
     */
    fun parseExpression(): Expression {
        return parseAdditive()
    }

    /**
     * Parse an additive expression (term + term, term - term)
     *
     * @return Expression AST node
     */
    private fun parseAdditive(): Expression {
        var left = parseMultiplicative()

        while (match(TokenType.PLUS) || match(TokenType.MINUS)) {
            val operator = previous().type
            val right = parseMultiplicative()

            left = when (operator) {
                TokenType.PLUS -> Expression.Add(left, right)
                TokenType.MINUS -> Expression.Sub(left, right)
                else -> throw RuntimeException("Unexpected operator: ${previous().value}")
            }
        }

        return left
    }

    /**
     * Parse a multiplicative expression (factor * factor, factor / factor, factor % factor)
     *
     * @return Expression AST node
     */
    private fun parseMultiplicative(): Expression {
        var left = parsePrimary()

        while (match(TokenType.MUL) || match(TokenType.DIV) || match(TokenType.MOD)) {
            val operator = previous().type
            val right = parsePrimary()

            left = when (operator) {
                TokenType.MUL -> Expression.Mul(left, right)
                TokenType.DIV -> Expression.Div(left, right)
                TokenType.MOD -> Expression.Mod(left, right)
                else -> throw RuntimeException("Unexpected operator: ${previous().value}")
            }
        }

        return left
    }

    /**
     * Parse a primary expression (literal, variable reference, or list)
     *
     * @return Expression AST node
     */
    private fun parsePrimary(): Expression {
        val token = peek()

        return when (token.type) {
            TokenType.INT_LITERAL -> {
                advance()
                Expression.IntLiteral(token.value.toInt())
            }
            TokenType.IDENTIFIER -> {
                advance()
                Expression.VariableRef(token.value)
            }
            TokenType.LIST -> {
                advance()
                Expression.List("Object")
            }
            else -> {
                errorReporter?.reportSyntaxError("Unexpected token: ${token.value}", token.line, token.column)
                // Return a dummy expression to allow parsing to continue
                Expression.IntLiteral(0)
            }
        }
    }

    /**
     * Check if the current token matches the expected type and advance if it does
     *
     * @param type Expected token type
     * @return true if the current token matches the expected type
     */
    private fun match(type: TokenType): Boolean {
        if (isAtEnd()) return false
        if (peek().type != type) return false

        advance()
        return true
    }

    /**
     * Consume the current token if it matches the expected type, otherwise throw an error
     *
     * @param type Expected token type
     * @param errorMessage Error message to display if the token doesn't match
     * @return The consumed token
     */
    private fun consume(type: TokenType, errorMessage: String): Token {
        if (peek().type == type) {
            return advance()
        }

        val token = peek()
        errorReporter?.reportSyntaxError(errorMessage, token.line, token.column)

        // Advance to the next token to allow parsing to continue
        return advance()
    }

    /**
     * Advance to the next token and return the previous one
     *
     * @return The previous token
     */
    private fun advance(): Token {
        if (!isAtEnd()) currentPosition++
        return previous()
    }

    /**
     * Check if we've reached the end of the token stream
     *
     * @return true if we're at the end
     */
    private fun isAtEnd(): Boolean {
        return peek().type == TokenType.EOF
    }

    /**
     * Get the current token without advancing
     *
     * @return The current token
     */
    private fun peek(): Token {
        return tokens[currentPosition]
    }

    /**
     * Get the previous token
     *
     * @return The previous token
     */
    private fun previous(): Token {
        return tokens[currentPosition - 1]
    }
}
