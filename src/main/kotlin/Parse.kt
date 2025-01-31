package com.sasakirione

import kotlin.collections.ArrayDeque

/**
 * 与えられたソースコード文字列を行ごとに読み込み、Program (AST) を返す簡易パーサ。
 */
fun parseProgram(source: String): Program {
    // 改行で行に分割 & 前後の空白除去 & 空行除去
    val lines = source.split("\n", "\r\n").map { it.trim() }.filter { it.isNotEmpty() }

    val statements = lines.map { parseLine(it) }
    return Program(statements)
}

/**
 * 1行をパースして Statement を作る。
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
 * 簡易的な式パーサ。
 * "a + b" か、単項 (整数 or 変数名) だけをサポート。
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
            // val op2 = operatorStack.lastOrNull()
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
        val res = when (op1) {
            Keywords.PLUS -> Expression.Add(v2, v1)
            Keywords.MINUS -> Expression.Sub(v2, v1)
            else -> {error("存在しない演算子です: $op1")}
        }
        valueStack.add(res)
    }
    return valueStack.last()
}

private fun isOperator(str: String): Boolean {
    return str == Keywords.PLUS || str == Keywords.MINUS
}

private fun splitExpressionText(exprStr: String): List<String> {
    val regex = Regex("""\d+|\w+|[${Regex.escape(Keywords.PLUS + Keywords.MINUS)}]""")
    return regex.findAll(exprStr).map { it.value.trim() }.toList()
}

/**
 * 単項をパース。整数リテラルか変数参照のどちらか。
 */
fun parseTerm(term: String): Expression {
    // 整数としてパースを試みる
    val intValue = term.toIntOrNull()
    return if (intValue != null) {
        Expression.IntLiteral(intValue)
    } else {
        // 変数参照
        Expression.VariableRef(term)
    }
}
