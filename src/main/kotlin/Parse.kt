package com.sasakirione

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
    // "a + b" のような単純形をサポート
    val parts = exprStr.split(Keywords.PLUS, Keywords.MINUS).map { it.trim() }
    return when (parts.size) {
        1 -> {
            // 単項
            parseTerm(parts[0])
        }
        2 -> {
            // a + b
            val left = parseTerm(parts[0])
            val right = parseTerm(parts[1])
            var isPlus = exprStr.contains(Keywords.PLUS)
            if (isPlus) {
                return Expression.Add(left, right)
            }
            //var isMinus = parts.contains(Keywords.MINUS)
            return Expression.Sub(left, right)
        }
        else -> {
            throw IllegalArgumentException("サポートされていない式です(複数の +): $exprStr")
        }
    }
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
