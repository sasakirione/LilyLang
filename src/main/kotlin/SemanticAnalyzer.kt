/**
 * Semantic analyzer for LilyLang
 * Performs semantic checks on the AST, such as variable declaration checks
 */
class SemanticAnalyzer(private val errorReporter: ErrorReporter? = null) {
    private val symbolTable = SymbolTable()

    /**
     * Analyze a program for semantic errors
     *
     * @param program The AST of the program to analyze
     * @return The same program if no errors are found
     * @throws RuntimeException if semantic errors are found
     */
    fun analyze(program: Program): Program {
        // Reset the symbol table
        symbolTable.enterScope()

        // Analyze each statement
        for (statement in program.statements) {
            analyzeStatement(statement)
        }

        return program
    }

    /**
     * Analyze a statement for semantic errors
     *
     * @param statement The statement to analyze
     * @throws RuntimeException if semantic errors are found
     */
    private fun analyzeStatement(statement: Statement) {
        when (statement) {
            is Statement.FunctionDecl -> {
                // Enter a new scope for the function
                symbolTable.enterScope()

                // Add parameters to the symbol table
                for (param in statement.params) {
                    // For simplicity, all parameters are treated as integers
                    symbolTable.declare(param, "int", 0, 0)
                }

                // Analyze the function body
                for (stmt in statement.body) {
                    analyzeStatement(stmt)
                }

                // Exit the function scope
                symbolTable.exitScope()
            }
            is Statement.ClassDecl -> {
                // Check if class is already declared
                if (symbolTable.isClassDeclared(statement.name)) {
                    errorReporter?.reportSemanticError("Class '${statement.name}' is already declared", 0, 0)
                    return
                }

                // Declare the class in the symbol table
                val classSymbol = symbolTable.declareClass(statement.name, 0, 0)

                // Enter a new scope for the class body
                symbolTable.enterScope()

                // Analyze the class members
                for (member in statement.members) {
                    when (member) {
                        is Statement.VarDecl -> {
                            // Analyze the expression
                            val exprType = analyzeExpression(member.expr)

                            // Declare the member in the class
                            classSymbol.declareMember(member.varName, exprType, 0, 0)
                        }
                        is Statement.FunctionDecl -> {
                            // For simplicity, all methods are treated as returning int
                            classSymbol.declareMember(member.name, "method", 0, 0)

                            // Analyze the method body
                            analyzeStatement(member)
                        }
                        else -> {
                            errorReporter?.reportSemanticError("Invalid class member: ${member.javaClass.simpleName}", 0, 0)
                        }
                    }
                }

                // Exit the class scope
                symbolTable.exitScope()
            }
            is Statement.VarDecl -> {
                // Check if variable is already declared
                if (symbolTable.lookup(statement.varName) != null) {
                    errorReporter?.reportSemanticError("Variable '${statement.varName}' is already declared", 0, 0)
                    return
                }

                // Analyze the expression
                val exprType = analyzeExpression(statement.expr)

                // Add variable to symbol table (using dummy line and column for now)
                symbolTable.declare(statement.varName, exprType, 0, 0)
            }

            is Statement.VarAssign -> {
                // Check if variable is declared
                val symbol = symbolTable.lookup(statement.varName)
                if (symbol == null) {
                    errorReporter?.reportSemanticError("Variable '${statement.varName}' is not declared", 0, 0)
                    return
                }

                // Analyse the expression
                val exprType = analyzeExpression(statement.expr)
                val varType = symbol.type

                // Check type compatibility (simple check for now)
                if (exprType != varType && varType != "Object") {
                    errorReporter?.reportSemanticError("Cannot assign ${exprType} to variable '${statement.varName}' of type ${varType}", 0, 0)
                    return
                }
            }

            is Statement.MemberAccess -> {
                // Analyze the object expression
                val objType = analyzeExpression(statement.obj)

                // Check if the object is a class instance
                if (!symbolTable.isClassDeclared(objType)) {
                    errorReporter?.reportSemanticError("Cannot access member '${statement.member}' of non-class type '${objType}'", 0, 0)
                    return
                }

                // Check if the member exists in the class
                val classSymbol = symbolTable.lookupClass(objType)
                if (classSymbol == null || !classSymbol.isMemberDeclared(statement.member)) {
                    errorReporter?.reportSemanticError("Member '${statement.member}' does not exist in class '${objType}'", 0, 0)
                    return
                }
            }

            is Statement.MemberAssign -> {
                // Analyze the object expression
                val objType = analyzeExpression(statement.obj)

                // Check if the object is a class instance
                if (!symbolTable.isClassDeclared(objType)) {
                    errorReporter?.reportSemanticError("Cannot assign to member '${statement.member}' of non-class type '${objType}'", 0, 0)
                    return
                }

                // Check if the member exists in the class
                val classSymbol = symbolTable.lookupClass(objType)
                if (classSymbol == null || !classSymbol.isMemberDeclared(statement.member)) {
                    errorReporter?.reportSemanticError("Member '${statement.member}' does not exist in class '${objType}'", 0, 0)
                    return
                }

                // Analyze the expression
                val exprType = analyzeExpression(statement.expr)

                // Check type compatibility (simple check for now)
                val memberSymbol = classSymbol.lookupMember(statement.member)
                if (memberSymbol != null && exprType != memberSymbol.type && memberSymbol.type != "Object") {
                    errorReporter?.reportSemanticError("Cannot assign ${exprType} to member '${statement.member}' of type ${memberSymbol.type}", 0, 0)
                    return
                }
            }

            is Statement.Print -> {
                // Just analyze the expression
                analyzeExpression(statement.expr)
            }

            is Statement.If -> {
                // Analyze the condition
                val conditionType = analyzeExpression(statement.condition)
                if (conditionType != "boolean") {
                    errorReporter?.reportSemanticError("Condition in if statement must be a boolean expression", 0, 0)
                }

                // Enter a new scope for the then branch
                symbolTable.enterScope()

                // Analyze the then branch
                for (stmt in statement.thenBranch) {
                    analyzeStatement(stmt)
                }

                // Exit the then branch scope
                symbolTable.exitScope()

                // If there's an else branch, analyze it too
                if (statement.elseBranch != null) {
                    // Enter a new scope for the else branch
                    symbolTable.enterScope()

                    // Analyze the else branch
                    for (stmt in statement.elseBranch) {
                        analyzeStatement(stmt)
                    }

                    // Exit the else branch scope
                    symbolTable.exitScope()
                }
            }

            is Statement.While -> {
                // Analyze the condition
                val conditionType = analyzeExpression(statement.condition)
                if (conditionType != "boolean") {
                    errorReporter?.reportSemanticError("Condition in while loop must be a boolean expression", 0, 0)
                }

                // Enter a new scope for the loop body
                symbolTable.enterScope()

                // Analyze the loop body
                for (stmt in statement.body) {
                    analyzeStatement(stmt)
                }

                // Exit the loop body scope
                symbolTable.exitScope()
            }

            is Statement.For -> {
                // Enter a new scope for the for loop
                symbolTable.enterScope()

                // Analyze the initialization if present
                if (statement.initialization != null) {
                    analyzeStatement(statement.initialization)
                }

                // Analyze the condition
                val conditionType = analyzeExpression(statement.condition)
                if (conditionType != "boolean") {
                    errorReporter?.reportSemanticError("Condition in for loop must be a boolean expression", 0, 0)
                }

                // Analyze the update if present
                if (statement.update != null) {
                    analyzeStatement(statement.update)
                }

                // Analyze the loop body
                for (stmt in statement.body) {
                    analyzeStatement(stmt)
                }

                // Exit the for loop scope
                symbolTable.exitScope()
            }
        }
    }

    /**
     * Analyse an expression and determine its type
     *
     * @param expression The expression to analyse
     * @return The type of the expression
     * @throws RuntimeException if semantic errors are found
     */
    private fun analyzeExpression(expression: Expression): String {
        return when (expression) {
            is Expression.IntLiteral -> "int"

            is Expression.BooleanLiteral -> "boolean"

            is Expression.StringLiteral -> "string"

            is Expression.VariableRef -> {
                // Check if variable is declared
                val symbol = symbolTable.lookup(expression.name)
                if (symbol == null) {
                    errorReporter?.reportSemanticError("Variable '${expression.name}' is not declared", 0, 0)
                    return "error"
                }

                // Return the variable's type
                return symbol.type
            }

            is Expression.ClassInstantiation -> {
                // Check if the class is declared
                if (!symbolTable.isClassDeclared(expression.className)) {
                    errorReporter?.reportSemanticError("Class '${expression.className}' is not declared", 0, 0)
                    return "error"
                }

                // Analyze all arguments
                for (arg in expression.args) {
                    analyzeExpression(arg)
                    // For simplicity, we won't check argument types
                }

                // Return the class name as the type
                return expression.className
            }

            is Expression.MemberAccess -> {
                // Analyze the object expression
                val objType = analyzeExpression(expression.obj)

                // Check if the object is a class instance
                if (!symbolTable.isClassDeclared(objType)) {
                    errorReporter?.reportSemanticError("Cannot access member '${expression.member}' of non-class type '${objType}'", 0, 0)
                    return "error"
                }

                // Check if the member exists in the class
                val classSymbol = symbolTable.lookupClass(objType)
                if (classSymbol == null || !classSymbol.isMemberDeclared(expression.member)) {
                    errorReporter?.reportSemanticError("Member '${expression.member}' does not exist in class '${objType}'", 0, 0)
                    return "error"
                }

                // Return the member's type
                val memberSymbol = classSymbol.lookupMember(expression.member)
                return memberSymbol?.type ?: "error"
            }

            is Expression.MethodCall -> {
                // Analyze the object expression
                val objType = analyzeExpression(expression.obj)

                // Check if the object is a class instance
                if (!symbolTable.isClassDeclared(objType)) {
                    errorReporter?.reportSemanticError("Cannot call method '${expression.method}' on non-class type '${objType}'", 0, 0)
                    return "error"
                }

                // Check if the method exists in the class
                val classSymbol = symbolTable.lookupClass(objType)
                if (classSymbol == null || !classSymbol.isMemberDeclared(expression.method)) {
                    errorReporter?.reportSemanticError("Method '${expression.method}' does not exist in class '${objType}'", 0, 0)
                    return "error"
                }

                // Analyze all arguments
                for (arg in expression.args) {
                    analyzeExpression(arg)
                    // For simplicity, we won't check argument types
                }

                // For simplicity, we'll assume all methods return int
                return "int"
            }

            is Expression.Add -> {
                val left = analyzeExpression(expression.left)
                val right = analyzeExpression(expression.right)

                // Special case for string concatenation
                if (left == "string" && right == "string") {
                    return "string"
                } else if (left == "string" || right == "string") {
                    // Allow string + int or int + string
                    if (left == "int" || right == "int") {
                        return "string"
                    }
                    errorReporter?.reportSemanticError("Cannot concatenate string with ${if (left == "string") right else left}", 0, 0)
                    return "error"
                } else if (left != "int" || right != "int") {
                    // For non-string operands, require integers
                    errorReporter?.reportSemanticError("Arithmetic addition requires integer operands", 0, 0)
                    return "error"
                }

                return "int"
            }

            is Expression.Sub, is Expression.Mul, is Expression.Div, is Expression.Mod -> {
                val left = when (expression) {
                    is Expression.Sub -> analyzeExpression(expression.left)
                    is Expression.Mul -> analyzeExpression(expression.left)
                    is Expression.Div -> analyzeExpression(expression.left)
                    is Expression.Mod -> analyzeExpression(expression.left)
                    else -> throw IllegalStateException("Unreachable code")
                }

                val right = when (expression) {
                    is Expression.Sub -> analyzeExpression(expression.right)
                    is Expression.Mul -> analyzeExpression(expression.right)
                    is Expression.Div -> analyzeExpression(expression.right)
                    is Expression.Mod -> analyzeExpression(expression.right)
                    else -> throw IllegalStateException("Unreachable code")
                }

                // Check if both operands are integers
                if (left != "int" || right != "int") {
                    errorReporter?.reportSemanticError("Arithmetic operations require integer operands", 0, 0)
                    return "error"
                }

                "int"
            }

            is Expression.And, is Expression.Or -> {
                val left = when (expression) {
                    is Expression.And -> analyzeExpression(expression.left)
                    is Expression.Or -> analyzeExpression(expression.left)
                    else -> throw IllegalStateException("Unreachable code")
                }

                val right = when (expression) {
                    is Expression.And -> analyzeExpression(expression.right)
                    is Expression.Or -> analyzeExpression(expression.right)
                    else -> throw IllegalStateException("Unreachable code")
                }

                // Check if both operands are booleans
                if (left != "boolean" || right != "boolean") {
                    errorReporter?.reportSemanticError("Logical operations require boolean operands", 0, 0)
                    return "error"
                }

                "boolean"
            }

            is Expression.Not -> {
                val operand = analyzeExpression(expression.expr)

                // Check if the operand is a boolean
                if (operand != "boolean") {
                    errorReporter?.reportSemanticError("Logical NOT operation requires a boolean operand", 0, 0)
                    return "error"
                }

                "boolean"
            }

            // Comparison expressions
            is Expression.Equal, is Expression.NotEqual -> {
                val left = when (expression) {
                    is Expression.Equal -> analyzeExpression(expression.left)
                    is Expression.NotEqual -> analyzeExpression(expression.left)
                    else -> throw IllegalStateException("Unreachable code")
                }

                val right = when (expression) {
                    is Expression.Equal -> analyzeExpression(expression.right)
                    is Expression.NotEqual -> analyzeExpression(expression.right)
                    else -> throw IllegalStateException("Unreachable code")
                }

                // Check if operands are of the same type
                if (left != right && left != "error" && right != "error") {
                    errorReporter?.reportSemanticError("Cannot compare values of different types: $left and $right", 0, 0)
                    return "error"
                }

                "boolean"
            }

            // Numeric comparison expressions
            is Expression.LessThan, is Expression.GreaterThan, is Expression.LessEqual, is Expression.GreaterEqual -> {
                val left = when (expression) {
                    is Expression.LessThan -> analyzeExpression(expression.left)
                    is Expression.GreaterThan -> analyzeExpression(expression.left)
                    is Expression.LessEqual -> analyzeExpression(expression.left)
                    is Expression.GreaterEqual -> analyzeExpression(expression.left)
                    else -> throw IllegalStateException("Unreachable code")
                }

                val right = when (expression) {
                    is Expression.LessThan -> analyzeExpression(expression.right)
                    is Expression.GreaterThan -> analyzeExpression(expression.right)
                    is Expression.LessEqual -> analyzeExpression(expression.right)
                    is Expression.GreaterEqual -> analyzeExpression(expression.right)
                    else -> throw IllegalStateException("Unreachable code")
                }

                // Check if both operands are integers
                if (left != "int" || right != "int") {
                    errorReporter?.reportSemanticError("Numeric comparison requires integer operands", 0, 0)
                    return "error"
                }

                "boolean"
            }

            is Expression.List -> "Object"

            is Expression.FunctionCall -> {
                // For simplicity, we'll assume all functions return int
                // In a more sophisticated implementation, we would look up the function in a function table

                // Analyze all arguments
                for (arg in expression.args) {
                    val argType = analyzeExpression(arg)
                    // For simplicity, we'll assume all arguments should be int
                    if (argType != "int") {
                        errorReporter?.reportSemanticError("Function argument must be an integer", 0, 0)
                    }
                }

                "int"
            }
        }
    }
}
