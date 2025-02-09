package com.sasakirione

import kotlin.collections.ArrayDeque

/**
 * ソースコードを解析し、AST（抽象構文木）の`Program`オブジェクトを生成する。
 *
 * @param source ソースコード全体を表す文字列
 * @return 解析された`Program`オブジェクト
 */
fun parseProgram(source: String): Program {
    // 改行で行に分割 & 前後の空白除去 & 空行除去
    val lines = source.split("\n", "\r\n").map { it.trim() }.filter { it.isNotEmpty() }

    val statements = lines.map { parseLine(it) }
    return Program(statements)
}

/**
 * 指定された文字列を解析し、対応するStatementオブジェクトを生成する。
 *
 * @param line 処理する文字列(ステートメント)
 * @return 解析されたStatementオブジェクト
 */
fun parseLine(line: String): Statement {
    return when {
        line.startsWith(Keywords.VAR) -> {
            // 例: "var x = 10" -> varName="x", expr="10"
            val afterVar = line.removePrefix(Keywords.VAR).trim()
            val parts = afterVar.split(Keywords.EQUALS, limit = 2)
            require(parts.size == 2) { "構文エラー: var ステートメントに '=' がありません: $line" }
            val varName = parts[0].trim()
            val exprStr = parts[1].trim()
            Statement.VarDecl(varName, parseExpression(exprStr))
        }

        line.startsWith(Keywords.PRINT) -> {
            // 例: "print x + 10" -> expr="x + 10"
            val exprStr = line.removePrefix(Keywords.PRINT).trim()
            Statement.Print(parseExpression(exprStr))
        }

        else -> {
            // 例: "x = 10" -> parts[0]="x", parts[1]="10"
            val parts = line.split(Keywords.EQUALS).map { it.trim() }
            Statement.VarAssign(parts[0], parseExpression(parts[1]))
        }
    }
}


/**
 * 指定された式の文字列をパースし、それに対応する抽象構文木（AST）の Expression オブジェクトを生成する。
 *
 * @param exprStr 式を表す文字列
 * @return パースされた式を表す Expression オブジェクト
 */
fun parseExpression(exprStr: String): Expression {
    // 演算子とタームごととにリスト化する
    val elements = splitExpressionText(exprStr)
    val valueStack = ArrayDeque<Expression>()
    val operatorStack = ArrayDeque<String>()
    for (element in elements) {
        val isOperator = isOperator(element)
        if (isOperator) {
            val op1 = element
            var op2 = operatorStack.lastOrNull()
            while (op2 != null && (getOperatorPrecedence(op1) <= getOperatorPrecedence(op2))) {
                val op2Pop = operatorStack.removeLast()
                val v2 = valueStack.removeLast()
                val v1 = valueStack.removeLast()
                val res = getExpression(op2Pop, v1, v2)
                valueStack.add(res)
                op2 = operatorStack.lastOrNull()
            }
            // 割り算かけ算実装時はここにいろいろ
            operatorStack.add(op1)
            continue
        }
        valueStack.add(parseTerm(element))
    }
    while (operatorStack.isNotEmpty()) {
        val op1 = operatorStack.removeLast()
        val v1 = valueStack.removeLast()
        val v2 = valueStack.removeLast()
        val res = getExpression(op1, v2, v1)
        valueStack.add(res)
    }
    return valueStack.last()
}

/**
 * 演算子と2つの式から新しい式を生成します。
 *
 * @param op1 演算子を表す文字列。例: "+", "-", "*", "/", "%"
 * @param v2 左の式
 * @param v1 右の式
 * @return 計算結果を表す新しい式
 * @throws IllegalArgumentException 無効な演算子が指定された場合
 */
private fun getExpression(
    op1: String,
    v2: Expression,
    v1: Expression
): Expression {
    val res = when (op1) {
        Keywords.PLUS -> Expression.Add(v2, v1)
        Keywords.MINUS -> Expression.Sub(v2, v1)
        Keywords.MOD -> Expression.Mod(v2, v1)
        Keywords.MUL -> Expression.Mul(v2, v1)
        Keywords.DIV -> Expression.Div(v2, v1)
        else -> {
            error("存在しない演算子です: $op1")
        }
    }
    return res
}

private fun isOperator(str: String): Boolean {
    return str == Keywords.PLUS || str == Keywords.MINUS || str == Keywords.MOD || str == Keywords.MUL || str == Keywords.DIV
}

/**
 * 与えられた演算子の優先順位を決定する
 *
 * @param op 文字列としての演算子（例：「+」、「-」、「*」、「/」、「%」）。
 * @return 演算子の優先順位を示す整数。
 * 値が大きいほど優先順位が高いことを示す。認識できない演算子の場合は0を返します。
 */
private fun getOperatorPrecedence(op: String): Int {
    return when (op) {
        Keywords.PLUS, Keywords.MINUS -> 1
        Keywords.MOD, Keywords.MUL, Keywords.DIV -> 2
        else -> 0
    }
}

/**
 * 式の文字列を演算子とそれ以外でパースする
 * 12+1*3 -> 12,+,1,*,3
 *
 * @param exprStr 式の文字列
 * @return パースしたリスト
 */
private fun splitExpressionText(exprStr: String): List<String> {
    val regex = Regex("""\d+|\w+|[${Regex.escape(Keywords.PLUS + Keywords.MINUS + Keywords.MOD + Keywords.MUL + Keywords.DIV)}]""")
    return regex.findAll(exprStr).map { it.value.trim() }.toList()
}

/**
 * 与えられた文字列を解析して適切な式(Expression)を生成します。
 *
 * @param term 解析対象の文字列
 * @return 解析結果に基づく適切なExpressionインスタンス
 */
fun parseTerm(term: String): Expression {
    val intValue = term.toIntOrNull()
    val isList = term.startsWith(Keywords.LIST)
    return if (intValue != null) {
        Expression.IntLiteral(intValue)
    } else if (isList) {
        Expression.List("Object")
    } else {
        // 変数参照
        Expression.VariableRef(term)
    }
}
