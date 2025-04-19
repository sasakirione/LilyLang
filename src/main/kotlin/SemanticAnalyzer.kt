import com.sasakirione.Expression
import com.sasakirione.Program
import com.sasakirione.Statement


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

            is Statement.Print -> {
                // Just analyze the expression
                analyzeExpression(statement.expr)
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

            is Expression.Add, is Expression.Sub, is Expression.Mul, is Expression.Div, is Expression.Mod -> {
                val left = when (expression) {
                    is Expression.Add -> analyzeExpression(expression.left)
                    is Expression.Sub -> analyzeExpression(expression.left)
                    is Expression.Mul -> analyzeExpression(expression.left)
                    is Expression.Div -> analyzeExpression(expression.left)
                    is Expression.Mod -> analyzeExpression(expression.left)
                    else -> throw IllegalStateException("Unreachable code")
                }

                val right = when (expression) {
                    is Expression.Add -> analyzeExpression(expression.right)
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

            is Expression.List -> "Object"
        }
    }
}
