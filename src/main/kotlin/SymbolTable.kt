/**
 * Symbol table for LilyLang
 * Tracks variables, their types, and their scopes
 */
class SymbolTable {
    // Stack of scopes, where each scope is a map of variable names to symbols
    private val scopes = mutableListOf<MutableMap<String, Symbol>>()

    // Map of class names to class symbols
    private val classes = mutableMapOf<String, ClassSymbol>()

    init {
        // Initialise with global scope
        enterScope()
    }

    /**
     * Enter a new scope (e.g. when entering a block or function)
     */
    fun enterScope() {
        scopes.add(mutableMapOf())
    }

    /**
     * Exit the current scope (e.g. when exiting a block or function)
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

    /**
     * Declare a class in the symbol table
     * @param name The name of the class
     * @param line The line where the class is declared
     * @param column The column where the class is declared
     * @return The created class symbol
     * @throws RuntimeException if the class is already declared
     */
    fun declareClass(name: String, line: Int, column: Int): ClassSymbol {
        if (classes.containsKey(name)) {
            throw RuntimeException("Class '$name' is already declared at line $line, column $column")
        }

        val classSymbol = ClassSymbol(name, line, column)
        classes[name] = classSymbol
        return classSymbol
    }

    /**
     * Look up a class in the symbol table
     * @param name The name of the class
     * @return The class symbol, or null if not found
     */
    fun lookupClass(name: String): ClassSymbol? {
        return classes[name]
    }

    /**
     * Check if a class is declared
     * @param name The name of the class
     * @return true if the class is declared
     */
    fun isClassDeclared(name: String): Boolean {
        return classes.containsKey(name)
    }

    /**
     * Get all classes
     * @return A list of all class symbols
     */
    fun getAllClasses(): List<ClassSymbol> {
        return classes.values.toList()
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

/**
 * Represents a class symbol in the symbol table
 * @property name The name of the class
 * @property line The line where the class is declared
 * @property column The column where the class is declared
 * @property members The members of the class (variables and methods)
 */
data class ClassSymbol(
    val name: String,
    val line: Int,
    val column: Int
) {
    // Map of member names to member symbols
    val members = mutableMapOf<String, Symbol>()

    /**
     * Declare a member in the class
     * @param name The name of the member
     * @param type The type of the member
     * @param line The line where the member is declared
     * @param column The column where the member is declared
     * @return The created symbol
     * @throws RuntimeException if the member is already declared in the class
     */
    fun declareMember(name: String, type: String, line: Int, column: Int): Symbol {
        if (members.containsKey(name)) {
            throw RuntimeException("Member '$name' is already declared in class '$this.name' at line $line, column $column")
        }

        val symbol = Symbol(name, type, line, column)
        members[name] = symbol
        return symbol
    }

    /**
     * Look up a member in the class
     * @param name The name of the member
     * @return The symbol for the member, or null if not found
     */
    fun lookupMember(name: String): Symbol? {
        return members[name]
    }

    /**
     * Check if a member is declared in the class
     * @param name The name of the member
     * @return true if the member is declared in the class
     */
    fun isMemberDeclared(name: String): Boolean {
        return members.containsKey(name)
    }

    /**
     * Get all members of the class
     * @return A list of all member symbols
     */
    fun getAllMembers(): List<Symbol> {
        return members.values.toList()
    }
}
