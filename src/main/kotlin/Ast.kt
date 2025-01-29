package com.sasakirione

/* ==============================
 *  1) AST 定義
 * ============================== */

/**
 * プログラム全体: 複数の文(Statement)からなる。
 */
data class Program(
    val statements: List<Statement>
)

/**
 * 文の種類。
 * 今回は「変数定義(var x = expr)」「print(expr)」のみ。
 */
sealed class Statement {
    /**
     * 変数定義
     */
    data class VarDecl(val varName: String, val expr: Expression) : Statement()

    /**
     * 変数再代入
     */
    data class VarAssign(val varName: String, val expr: Expression) : Statement()

    /**
     * 標準出力
     */
    data class Print(val expr: Expression) : Statement()
}

/**
 * 式の種類。
 * - 整数リテラル
 * - 変数参照
 * - 加算式 (ただし a + b の単純な形のみ)
 */
sealed class Expression {
    /**
     * 整数リテラル
     */
    data class IntLiteral(val value: Int) : Expression()

    /**
     * 変数宣言子
     */
    data class VariableRef(val name: String) : Expression()

    /**
     * 加算
     */
    data class Add(val left: Expression, val right: Expression) : Expression()

    /**
     * 減産
     */
    data class Sub(val left: Expression, val right: Expression) : Expression()
}

object Keywords {
    const val VAR = "var"
    const val PRINT = "print"
    const val PLUS = "+"
    const val MINUS = "-"
    const val EQUALS = "="
}