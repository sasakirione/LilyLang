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
 * 変数定義、変数再代入、標準出力、制御構造（if/else, while, for）
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

    /**
     * If文 (条件分岐)
     * condition: 条件式
     * thenBranch: 条件が真の場合に実行される文のリスト
     * elseBranch: 条件が偽の場合に実行される文のリスト (省略可能)
     */
    data class If(
        val condition: Expression,
        val thenBranch: List<Statement>,
        val elseBranch: List<Statement>? = null
    ) : Statement()

    /**
     * While文 (繰り返し)
     * condition: ループ継続条件
     * body: ループ本体の文のリスト
     */
    data class While(
        val condition: Expression,
        val body: List<Statement>
    ) : Statement()

    /**
     * For文 (繰り返し)
     * initialization: 初期化式 (省略可能)
     * condition: ループ継続条件
     * update: 更新式 (省略可能)
     * body: ループ本体の文のリスト
     */
    data class For(
        val initialization: Statement?,
        val condition: Expression,
        val update: Statement?,
        val body: List<Statement>
    ) : Statement()
}

/**
 * 式の種類。
 * - 整数リテラル
 * - 真偽値リテラル
 * - 変数参照
 * - 加算式 (ただし a + b の単純な形のみ)
 * - 論理演算 (AND, OR, NOT)
 * - 比較演算 (==, !=, <, >, <=, >=)
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
     * 文字列リテラル
     */
    data class StringLiteral(val value: String) : Expression()

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
     * 等価比較 (==)
     */
    data class Equal(val left: Expression, val right: Expression) : Expression()

    /**
     * 非等価比較 (!=)
     */
    data class NotEqual(val left: Expression, val right: Expression) : Expression()

    /**
     * 小なり比較 (<)
     */
    data class LessThan(val left: Expression, val right: Expression) : Expression()

    /**
     * 大なり比較 (>)
     */
    data class GreaterThan(val left: Expression, val right: Expression) : Expression()

    /**
     * 以下比較 (<=)
     */
    data class LessEqual(val left: Expression, val right: Expression) : Expression()

    /**
     * 以上比較 (>=)
     */
    data class GreaterEqual(val left: Expression, val right: Expression) : Expression()

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

    // Control flow keywords
    const val IF = "if"
    const val ELSE = "else"
    const val WHILE = "while"
    const val FOR = "for"

    // Comparison operators
    const val EQUALS_EQUALS = "=="
    const val NOT_EQUALS = "!="
    const val LESS_EQUALS = "<="
    const val GREATER_EQUALS = ">="

    // Delimiters
    const val LEFT_BRACE = '{'
    const val RIGHT_BRACE = '}'
    const val SEMICOLON = ';'

    // Boolean literals
    const val TRUE = "true"
    const val FALSE = "false"

    // Logical operators
    const val AND = "and"
    const val OR = "or"
    const val NOT = "not"
}
