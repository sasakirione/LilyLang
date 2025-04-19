import com.sasakirione.Expression
import com.sasakirione.Program
import com.sasakirione.Statement
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Code generator for LilyLang
 * Converts an AST into JVM bytecode
 */
class CodeGenerator {
    /**
     * Generate bytecode for a program
     *
     * @param program The AST of the program
     * @param className The name of the class to generate
     * @return The generated bytecode as a byte array
     */
    fun generate(program: Program, className: String): ByteArray {
        val cw = ClassWriter(0)

        // Class definition: public class <className> extends Object
        cw.visit(
            Opcodes.V24,
            Opcodes.ACC_PUBLIC,
            className,
            null,
            "java/lang/Object",
            null
        )

        // Default constructor (public <init>())
        generateConstructor(cw)

        // Main method: public static void main(String[] args)
        generateMainMethod(cw, program)

        // Finish class definition
        cw.visitEnd()

        // Return bytecode as byte array
        return cw.toByteArray()
    }

    /**
     * Generate the default constructor
     *
     * @param cw The ClassWriter to use
     */
    private fun generateConstructor(cw: ClassWriter) {
        val mv = cw.visitMethod(
            Opcodes.ACC_PUBLIC,
            "<init>",
            "()V",
            null,
            null
        )
        mv.visitCode()
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Object",
            "<init>",
            "()V",
            false
        )
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
    }

    /**
     * Generate the main method
     *
     * @param cw The ClassWriter to use
     * @param program The AST of the program
     */
    private fun generateMainMethod(cw: ClassWriter, program: Program) {
        val mv = cw.visitMethod(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
            "main",
            "([Ljava/lang/String;)V",
            null,
            null
        )
        mv.visitCode()

        // Variable name -> local variable number mapping
        // main's local variable 0 is for args
        val varIndexMap = mutableListOf<VariableDetail>()
        var nextLocalIndex = 1

        // Generate code for each statement
        for (stmt in program.statements) {
            when (stmt) {
                is Statement.VarDecl -> {
                    val isExist = varIndexMap.any { it.name == stmt.varName }
                    if (isExist) {
                        error("Variable redefinition not allowed: ${stmt.varName}")
                    }

                    // Generate code for the expression
                    generateExpression(stmt.expr, mv, varIndexMap)

                    // Store the value in a local variable
                    varIndexMap.add(VariableDetail("int", nextLocalIndex, stmt.varName))
                    when (stmt.expr) {
                        is Expression.List -> {
                            // For ArrayList instances (object references), use ASTORE
                            mv.visitVarInsn(Opcodes.ASTORE, nextLocalIndex)
                        }
                        else -> {
                            // For integers and other primitives, use ISTORE
                            mv.visitVarInsn(Opcodes.ISTORE, nextLocalIndex)
                        }
                    }
                    nextLocalIndex++
                }

                is Statement.Print -> {
                    // Generate code for the expression
                    generateExpression(stmt.expr, mv, varIndexMap)

                    // Print the value: System.out.println(int)
                    mv.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        "java/lang/System",
                        "out",
                        "Ljava/io/PrintStream;"
                    )
                    // Stack has [int, PrintStream], so swap them
                    mv.visitInsn(Opcodes.SWAP)
                    mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/io/PrintStream",
                        "println",
                        "(I)V",
                        false
                    )
                }

                is Statement.VarAssign -> {
                    val varDetail = varIndexMap.firstOrNull { it.name == stmt.varName }
                        ?: error("Variable not declared: ${stmt.varName}")

                    // Generate code for the expression
                    generateExpression(stmt.expr, mv, varIndexMap)

                    // Store the value in the variable
                    mv.visitVarInsn(Opcodes.ISTORE, varDetail.index)
                }
            }
        }

        // End of main method
        mv.visitInsn(Opcodes.RETURN)
        // Set stack size and local variable size
        mv.visitMaxs(2, nextLocalIndex)
        mv.visitEnd()
    }

    /**
     * Generate code for an expression
     *
     * @param expr The expression to generate code for
     * @param mv The MethodVisitor to use
     * @param varIndexMap The variable index map
     */
    private fun generateExpression(
        expr: Expression,
        mv: MethodVisitor,
        varIndexMap: MutableList<VariableDetail>
    ) {
        when (expr) {
            is Expression.IntLiteral -> {
                // Push integer literal onto the stack
                mv.visitLdcInsn(expr.value)
            }
            is Expression.VariableRef -> {
                // Load variable value onto the stack
                val detail = varIndexMap.firstOrNull { it.name == expr.name }
                    ?: error("Undefined variable: ${expr.name}")
                mv.visitVarInsn(Opcodes.ILOAD, detail.index)
            }
            is Expression.Add -> {
                // Generate code for left and right operands, then add
                generateExpression(expr.left, mv, varIndexMap)
                generateExpression(expr.right, mv, varIndexMap)
                mv.visitInsn(Opcodes.IADD)
            }
            is Expression.Sub -> {
                // Generate code for left and right operands, then subtract
                generateExpression(expr.left, mv, varIndexMap)
                generateExpression(expr.right, mv, varIndexMap)
                mv.visitInsn(Opcodes.ISUB)
            }
            is Expression.Mul -> {
                // Generate code for left and right operands, then multiply
                generateExpression(expr.left, mv, varIndexMap)
                generateExpression(expr.right, mv, varIndexMap)
                mv.visitInsn(Opcodes.IMUL)
            }
            is Expression.Div -> {
                // Generate code for left and right operands, then divide
                generateExpression(expr.left, mv, varIndexMap)
                generateExpression(expr.right, mv, varIndexMap)
                mv.visitInsn(Opcodes.IDIV)
            }
            is Expression.Mod -> {
                // Generate code for left and right operands, then modulo
                generateExpression(expr.left, mv, varIndexMap)
                generateExpression(expr.right, mv, varIndexMap)
                mv.visitInsn(Opcodes.IREM)
            }
            is Expression.List -> {
                // Create a new ArrayList
                mv.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList")
                mv.visitInsn(Opcodes.DUP) // Duplicate the reference
                mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    "java/util/ArrayList",
                    "<init>",
                    "()V",
                    false
                )
            }
        }
    }
}