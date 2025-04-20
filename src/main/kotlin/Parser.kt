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
            TokenType.IF -> parseIfStatement()
            TokenType.WHILE -> parseWhileStatement()
            TokenType.FOR -> parseForStatement()
            TokenType.FUN -> parseFunctionDeclaration()
            else -> {
                errorReporter?.reportSyntaxError("Unexpected token: ${token.value}", token.line, token.column)
                // Return a fake statement to allow parsing to continue
                Statement.Print(Expression.IntLiteral(0))
            }
        }
    }

    /**
     * Parse a function declaration
     *
     * @return FunctionDecl statement
     */
    private fun parseFunctionDeclaration(): Statement {
        // Consume 'fun' token
        advance()

        // The Next token should be an identifier (function name)
        val functionName = consume(TokenType.IDENTIFIER, "Expected function name after 'fun'").value

        // The Next token should be '('
        consume(TokenType.LEFT_PAREN, "Expected '(' after function name")

        // Parse parameter list
        val parameters = mutableListOf<String>()
        if (peek().type != TokenType.RIGHT_PAREN) {
            do {
                val paramName = consume(TokenType.IDENTIFIER, "Expected parameter name").value
                parameters.add(paramName)

                if (peek().type != TokenType.COMMA) {
                    break
                }

                // Consume the comma
                advance()
            } while (true)
        }

        // Expect closing parenthesis
        consume(TokenType.RIGHT_PAREN, "Expected ')' after parameter list")

        // Parse function body
        val body = parseBlock()

        return Statement.FunctionDecl(functionName, parameters, body)
    }

    /**
     * Parse a block of statements enclosed in braces
     *
     * @return List of statements in the block
     */
    private fun parseBlock(): List<Statement> {
        val statements = mutableListOf<Statement>()

        // Expect an opening brace
        consume(TokenType.LEFT_BRACE, "Expected '{' at the start of a block")

        // Parse statements until we reach a closing brace or the end of the file
        while (!isAtEnd() && peek().type != TokenType.RIGHT_BRACE) {
            statements.add(parseStatement())
        }

        // Expect a closing brace
        consume(TokenType.RIGHT_BRACE, "Expected '}' at the end of a block")

        return statements
    }

    /**
     * Parse an if statement
     *
     * @return If statement
     */
    private fun parseIfStatement(): Statement {
        // Consume 'if' token
        advance()

        // Parse condition
        val condition = parseExpression()

        // Parse then branch
        val thenBranch = parseBlock()

        // Check for else branch
        var elseBranch: List<Statement>? = null
        if (peek().type == TokenType.ELSE) {
            advance() // Consume 'else' token
            elseBranch = parseBlock()
        }

        return Statement.If(condition, thenBranch, elseBranch)
    }

    /**
     * Parse a while statement
     *
     * @return While statement
     */
    private fun parseWhileStatement(): Statement {
        // Consume 'while' token
        advance()

        // Parse condition
        val condition = parseExpression()

        // Parse body
        val body = parseBlock()

        return Statement.While(condition, body)
    }

    /**
     * Parse a for statement
     *
     * @return For statement
     */
    private fun parseForStatement(): Statement {
        // Consume 'for' token
        advance()

        // Parse initialization (optional)
        var initialization: Statement? = null
        if (peek().type != TokenType.SEMICOLON) {
            initialization = parseStatement()
        } else {
            advance() // Consume ';'
        }

        // Parse condition
        val condition = parseExpression()

        // Expect a semicolon
        consume(TokenType.SEMICOLON, "Expected ';' after for loop condition")

        // Parse update (optional)
        var update: Statement? = null
        if (peek().type != TokenType.LEFT_BRACE) {
            update = parseStatement()
        }

        // Parse body
        val body = parseBlock()

        return Statement.For(initialization, condition, update, body)
    }

    /**
     * Parse a variable declaration statement (var x = expr)
     *
     * @return VarDecl statement
     */
    private fun parseVarDeclaration(): Statement {
        // Consume 'var' token
        advance()

        // The Next token should be an identifier
        val varName = consume(TokenType.IDENTIFIER, "Expected variable name after 'var'").value

        // The Next token should be '='
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

        // The Next token should be '='
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
        return parseLogical()
    }

    /**
     * Parse a logical expression (expr AND expr, expr OR expr)
     *
     * @return Expression AST node
     */
    private fun parseLogical(): Expression {
        var left = parseComparison()

        while (match(TokenType.AND) || match(TokenType.OR)) {
            val operator = previous().type
            val right = parseComparison()

            left = when (operator) {
                TokenType.AND -> Expression.And(left, right)
                TokenType.OR -> Expression.Or(left, right)
                else -> throw RuntimeException("Unexpected operator: ${previous().value}")
            }
        }

        return left
    }

    /**
     * Parse a comparison expression (expr == expr, expr != expr, expr < expr, etc.)
     *
     * @return Expression AST node
     */
    private fun parseComparison(): Expression {
        var left = parseAdditive()

        while (match(TokenType.EQUALS_EQUALS) || match(TokenType.NOT_EQUALS) || 
               match(TokenType.LESS_THAN) || match(TokenType.GREATER_THAN) ||
               match(TokenType.LESS_EQUALS) || match(TokenType.GREATER_EQUALS)) {
            val operator = previous().type
            val right = parseAdditive()

            left = when (operator) {
                TokenType.EQUALS_EQUALS -> Expression.Equal(left, right)
                TokenType.NOT_EQUALS -> Expression.NotEqual(left, right)
                TokenType.LESS_THAN -> Expression.LessThan(left, right)
                TokenType.GREATER_THAN -> Expression.GreaterThan(left, right)
                TokenType.LESS_EQUALS -> Expression.LessEqual(left, right)
                TokenType.GREATER_EQUALS -> Expression.GreaterEqual(left, right)
                else -> throw RuntimeException("Unexpected operator: ${previous().value}")
            }
        }

        return left
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
            TokenType.BOOLEAN_LITERAL -> {
                advance()
                Expression.BooleanLiteral(token.value == Keywords.TRUE)
            }
            TokenType.STRING_LITERAL -> {
                advance()
                Expression.StringLiteral(token.value)
            }
            TokenType.IDENTIFIER -> {
                advance()

                // Check if this is a function call
                if (peek().type == TokenType.LEFT_PAREN) {
                    // This is a function call
                    val functionName = token.value

                    // Consume the '('
                    advance()

                    // Parse arguments
                    val arguments = mutableListOf<Expression>()
                    if (peek().type != TokenType.RIGHT_PAREN) {
                        do {
                            arguments.add(parseExpression())

                            if (peek().type != TokenType.COMMA) {
                                break
                            }

                            // Consume the comma
                            advance()
                        } while (true)
                    }

                    // Expect closing parenthesis
                    consume(TokenType.RIGHT_PAREN, "Expected ')' after function arguments")

                    Expression.FunctionCall(functionName, arguments)
                } else {
                    // This is a variable reference
                    Expression.VariableRef(token.value)
                }
            }
            TokenType.LIST -> {
                advance()
                Expression.List("Object")
            }
            TokenType.NOT -> {
                advance()
                val expr = parsePrimary()
                Expression.Not(expr)
            }
            else -> {
                errorReporter?.reportSyntaxError("Unexpected token: ${token.value}", token.line, token.column)
                // Return a fake expression to allow parsing to continue
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
     * @return true, if we're at the end
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
