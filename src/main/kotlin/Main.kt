package com.sasakirione

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.FileOutputStream

/* ==============================
 *  3) バイトコード生成 (AST -> .class)
 * ============================== */

/**
 * AST (Program) をもとに、バイトコード(.class)を生成して返す。
 *
 * @param program   パース済みの AST
 * @param className 生成するクラス名 (例: "SimpleProgram")
 */
fun compileToBytecode(program: Program, className: String): ByteArray {
    val cw = ClassWriter(0)

    // クラス定義: public class <className> extends Object
    cw.visit(
        Opcodes.V1_8,
        Opcodes.ACC_PUBLIC,
        className,
        null,
        "java/lang/Object",
        null
    )

    // デフォルトコンストラクタ (public <init>())
    run {
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

    // main メソッド定義: public static void main(String[] args)
    val mv = cw.visitMethod(
        Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
        "main",
        "([Ljava/lang/String;)V",
        null,
        null
    )
    mv.visitCode()

    // 変数名 -> ローカル変数番号 のマッピング
    // main のローカル変数 0 は args 用
    val varIndexMap = mutableMapOf<String, Int>()
    var nextLocalIndex = 1

    // AST の文を順にコード生成
    for (stmt in program.statements) {
        when (stmt) {
            is Statement.VarDecl -> {
                var isExist = varIndexMap.keys.any{ it == stmt.varName }
                if (isExist) {
                    error("変数の再定義はできません: ${stmt.varName}")
                }

                // expr をスタックに積む
                generateExpression(stmt.expr, mv, varIndexMap)

                // スタックトップにある int をローカル変数に格納
                varIndexMap[stmt.varName] = nextLocalIndex
                mv.visitVarInsn(Opcodes.ISTORE, nextLocalIndex)
                nextLocalIndex++
            }

            is Statement.Print -> {
                // expr をスタックに積む
                generateExpression(stmt.expr, mv, varIndexMap)

                // print 処理: System.out.println(int)
                mv.visitFieldInsn(
                    Opcodes.GETSTATIC,
                    "java/lang/System",
                    "out",
                    "Ljava/io/PrintStream;"
                )
                // スタックには [int, PrintStream] の順で乗っているので swap で入れ替え
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
                val varIndex = varIndexMap[stmt.varName] ?:
                    error("変数が宣言されていません: ${stmt.varName}")

                generateExpression(stmt.expr, mv, varIndexMap)

                mv.visitVarInsn(Opcodes.ISTORE, varIndex)
            }
        }
    }

    // main メソッド終了
    mv.visitInsn(Opcodes.RETURN)
    // スタックサイズとローカル変数サイズを指定
    mv.visitMaxs(2, nextLocalIndex)
    mv.visitEnd()

    // クラス定義終了
    cw.visitEnd()

    // バイト配列として返す
    return cw.toByteArray()
}

/**
 * 式をコンパイルしてスタックに int を積む。
 */
fun generateExpression(
    expr: Expression,
    mv: MethodVisitor,
    varIndexMap: Map<String, Int>
) {
    when (expr) {
        is Expression.IntLiteral -> {
            // 整数リテラルをスタックに積む
            mv.visitLdcInsn(expr.value)
        }
        is Expression.VariableRef -> {
            // 変数をLOAD
            val index = varIndexMap[expr.name]
                ?: error("未定義の変数: ${expr.name}")
            mv.visitVarInsn(Opcodes.ILOAD, index)
        }
        is Expression.Add -> {
            // left, right を生成後、ADD
            generateExpression(expr.left, mv, varIndexMap)
            generateExpression(expr.right, mv, varIndexMap)
            mv.visitInsn(Opcodes.IADD)
        }
        is Expression.Sub -> {
            generateExpression(expr.left, mv, varIndexMap)
            generateExpression(expr.right, mv, varIndexMap)
            mv.visitInsn(Opcodes.ISUB)
        }
        is Expression.Mul -> {
            generateExpression(expr.left, mv, varIndexMap)
            generateExpression(expr.right, mv, varIndexMap)
            mv.visitInsn(Opcodes.IMUL)
        }
        is Expression.Div -> {
            generateExpression(expr.left, mv, varIndexMap)
            generateExpression(expr.right, mv, varIndexMap)
            mv.visitInsn(Opcodes.IDIV)
        }
        is Expression.Mod -> {
            generateExpression(expr.left, mv, varIndexMap)
            generateExpression(expr.right, mv, varIndexMap)
            mv.visitInsn(Opcodes.IREM)
        }
    }
}

/* ==============================
 *  4) 動作確認用 main 関数
 * ============================== */

fun main() {
    // サンプルの小さなプログラム
    val sampleSource = """
        var x = 10
        var y = 20
        var z = x + y - 10 + 100
        var exp1 = x * y
        var exp2 = 10 + x * y - 5
        var exp3 = 32 + x / y * z
        var exp4 = exp1 % 3
        z = 3
        print z
        print y + x
        print y - x
    """.trimIndent()

    // 1. ソースコードをパースしてASTを得る
    val programAst = parseProgram(sampleSource)

    // 2. ASTをバイトコードにコンパイル
    val className = "SimpleProgram"
    val bytecode = compileToBytecode(programAst, className)

    // 3. .classファイルに書き出し
    FileOutputStream("$className.class").use { it.write(bytecode) }

    println("コンパイル完了: $className.class を出力しました。")
    println("次のコマンドで実行してください: java $className")
}
