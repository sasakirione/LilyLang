/**
 * Symbol table for LilyLang
 * Tracks variables, their types, and their scopes
 */
class SymbolTable {
    // Stack of scopes, where each scope is a map of variable names to symbols
    private val scopes = mutableListOf<MutableMap<String, Symbol>>()
    
    init {
        // Initialize with global scope
        enterScope()
    }
    
    /**
     * Enter a new scope (e.g., when entering a block or function)
     */
    fun enterScope() {
        scopes.add(mutableMapOf())
    }
    
    /**
     * Exit the current scope (e.g., when exiting a block or function)
     * @throws IllegalStateException if trying to exit the global scope
     */
    fun exitScope() {
        if (scopes.size <= 1) {
            throw IllegalStateException("Cannot exit global scope")
        }
        scopes.removeAt(scopes.size - 1)
    }
    
    /**
     * Declare a variable in the current scope
     * @param name The name of the variable
     * @param type The type of the variable
     * @param line The line where the variable is declared
     * @param column The column where the variable is declared
     * @return The created symbol
     * @throws RuntimeException if the variable is already declared in the current scope
     */
    fun declare(name: String, type: String, line: Int, column: Int): Symbol {
        val currentScope = scopes.last()
        
        if (currentScope.containsKey(name)) {
            throw RuntimeException("Variable '$name' is already declared in the current scope at line $line, column $column")
        }
        
        val symbol = Symbol(name, type, line, column)
        currentScope[name] = symbol
        return symbol
    }
    
    /**
     * Look up a variable in all scopes, starting from the innermost
     * @param name The name of the variable
     * @return The symbol for the variable, or null if not found
     */
    fun lookup(name: String): Symbol? {
        // Search from innermost to outermost scope
        for (i in scopes.size - 1 downTo 0) {
            val scope = scopes[i]
            val symbol = scope[name]
            if (symbol != null) {
                return symbol
            }
        }
        return null
    }
    
    /**
     * Check if a variable is declared in the current scope
     * @param name The name of the variable
     * @return true if the variable is declared in the current scope
     */
    fun isDeclaredInCurrentScope(name: String): Boolean {
        return scopes.last().containsKey(name)
    }
    
    /**
     * Get all variables in all scopes
     * @return A list of all symbols
     */
    fun getAllSymbols(): List<Symbol> {
        val result = mutableListOf<Symbol>()
        for (scope in scopes) {
            result.addAll(scope.values)
        }
        return result
    }
}

/**
 * Represents a symbol (variable) in the symbol table
 * @property name The name of the variable
 * @property type The type of the variable
 * @property line The line where the variable is declared
 * @property column The column where the variable is declared
 */
data class Symbol(
    val name: String,
    val type: String,
    val line: Int,
    val column: Int
)