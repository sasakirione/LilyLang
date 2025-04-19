/**
 * Error reporter for LilyLang
 * Provides consistent error reporting with line and column information
 */
class ErrorReporter {
    private val errors = mutableListOf<CompilerError>()
    private var hasErrors = false
    
    /**
     * Report a lexical error
     *
     * @param message Error message
     * @param line Line number where the error occurred
     * @param column Column number where the error occurred
     */
    fun reportLexicalError(message: String, line: Int, column: Int) {
        errors.add(CompilerError(ErrorType.LEXICAL, message, line, column))
        hasErrors = true
    }
    
    /**
     * Report a syntax error
     *
     * @param message Error message
     * @param line Line number where the error occurred
     * @param column Column number where the error occurred
     */
    fun reportSyntaxError(message: String, line: Int, column: Int) {
        errors.add(CompilerError(ErrorType.SYNTAX, message, line, column))
        hasErrors = true
    }
    
    /**
     * Report a semantic error
     *
     * @param message Error message
     * @param line Line number where the error occurred
     * @param column Column number where the error occurred
     */
    fun reportSemanticError(message: String, line: Int, column: Int) {
        errors.add(CompilerError(ErrorType.SEMANTIC, message, line, column))
        hasErrors = true
    }
    
    /**
     * Check if any errors have been reported
     *
     * @return true if errors have been reported
     */
    fun hasErrors(): Boolean {
        return hasErrors
    }
    
    /**
     * Get all reported errors
     *
     * @return List of compiler errors
     */
    fun getErrors(): List<CompilerError> {
        return errors
    }
    
    /**
     * Print all errors to the console
     */
    fun printErrors() {
        for (error in errors) {
            println(formatError(error))
        }
    }
    
    /**
     * Format an error message
     *
     * @param error The compiler error to format
     * @return Formatted error message
     */
    private fun formatError(error: CompilerError): String {
        val typeStr = when (error.type) {
            ErrorType.LEXICAL -> "Lexical"
            ErrorType.SYNTAX -> "Syntax"
            ErrorType.SEMANTIC -> "Semantic"
        }
        
        return "$typeStr error at line ${error.line}, column ${error.column}: ${error.message}"
    }
    
    /**
     * Throw an exception if any errors have been reported
     *
     * @throws CompilationException if errors have been reported
     */
    fun throwIfErrors() {
        if (hasErrors) {
            val errorMessages = errors.joinToString("\n") { formatError(it) }
            throw CompilationException(errorMessages)
        }
    }
}

/**
 * Types of compiler errors
 */
enum class ErrorType {
    LEXICAL,
    SYNTAX,
    SEMANTIC
}

/**
 * Represents a compiler error
 *
 * @property type The type of error
 * @property message The error message
 * @property line The line number where the error occurred
 * @property column The column number where the error occurred
 */
data class CompilerError(
    val type: ErrorType,
    val message: String,
    val line: Int,
    val column: Int
)

/**
 * Exception thrown when compilation fails
 *
 * @param message The error message
 */
class CompilationException(message: String) : Exception(message)