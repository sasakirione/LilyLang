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
 * - 真偽値リテラル
 * - 変数参照
 * - 加算式 (ただし a + b の単純な形のみ)
 * - 論理演算 (AND, OR, NOT)
 */
sealed class Expression {
    /**
     * 整数リテラル
     */
    data class IntLiteral(val value: Int) : Expression()

    /**
     * 真偽値リテラル
     */
    data class BooleanLiteral(val value: Boolean) : Expression()

    /**
     * 変数宣言子
     */
    data class VariableRef(val name: String) : Expression()

    /**
     * 加算
     */
    data class Add(val left: Expression, val right: Expression) : Expression()

    /**
     * 減算
     */
    data class Sub(val left: Expression, val right: Expression) : Expression()

    /**
     * 乗算
     */
    data class Mul(val left: Expression, val right: Expression) : Expression()

    /**
     * 除算
     */
    data class Div(val left: Expression, val right: Expression) : Expression()

    /**
     * mod
     */
    data class Mod(val left: Expression, val right: Expression) : Expression()

    /**
     * 論理AND
     */
    data class And(val left: Expression, val right: Expression) : Expression()

    /**
     * 論理OR
     */
    data class Or(val left: Expression, val right: Expression) : Expression()

    /**
     * 論理NOT
     */
    data class Not(val expr: Expression) : Expression()

    /**
     * リスト
     */
    data class List(val type: String): Expression()
}

object Keywords {
    const val VAR = "var"
    const val PRINT = "print"
    const val PLUS = '+'
    const val MINUS = '-'
    const val MUL = '*'
    const val DIV = '/'
    const val MOD = '%'
    const val EQUALS = '='
    const val LESS_THAN = '<'
    const val GREATER_THAN = '>'
    const val LIST = "list"

    // Boolean literals
    const val TRUE = "true"
    const val FALSE = "false"

    // Logical operators
    const val AND = "and"
    const val OR = "or"
    const val NOT = "not"
}
