package com.sasakirione

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ParseKtTest {

    @Test
    @DisplayName("整数リテラルを解析する")
    fun parseExpression_test001() {
        val result = parseExpression("42")
        val expected = Expression.IntLiteral(42)
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("変数参照を解析する")
    fun parseExpression_test002() {
        val result = parseExpression("x")
        val expected = Expression.VariableRef("x")
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("加算式を解析する")
    fun parseExpression_test003() {
        val result = parseExpression("3 + 2")
        val expected = Expression.Add(
            Expression.IntLiteral(3),
            Expression.IntLiteral(2)
        )
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("減算式を解析する")
    fun parseExpression_test004() {
        val result = parseExpression("10 - 7")
        val expected = Expression.Sub(
            Expression.IntLiteral(10),
            Expression.IntLiteral(7)
        )
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("乗算式を解析する")
    fun parseExpression_test005() {
        val result = parseExpression("6 * 3")
        val expected = Expression.Mul(
            Expression.IntLiteral(6),
            Expression.IntLiteral(3)
        )
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("除算式を解析する")
    fun parseExpression_test006() {
        val result = parseExpression("12 / 4")
        val expected = Expression.Div(
            Expression.IntLiteral(12),
            Expression.IntLiteral(4)
        )
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("剰余算式を解析する")
    fun parseExpression_test007() {
        val result = parseExpression("15 % 4")
        val expected = Expression.Mod(
            Expression.IntLiteral(15),
            Expression.IntLiteral(4)
        )
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("加算と乗算の複合式を解析する")
    fun parseExpression_test008() {
        val result = parseExpression("2 + 3 * 4")
        val expected = Expression.Add(
            Expression.IntLiteral(2),
            Expression.Mul(
                Expression.IntLiteral(3),
                Expression.IntLiteral(4)
            )
        )
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("加算と減算の複合式を解析する")
    fun parseExpression_test009() {
        val result = parseExpression("8 - 4 + 2")
        val expected = Expression.Add(
            Expression.Sub(
                Expression.IntLiteral(8),
                Expression.IntLiteral(4)
            ),
            Expression.IntLiteral(2)
        )
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("複数変数の加算式を解析する")
    fun parseExpression_test010() {
        val result = parseExpression("x + y")
        val expected = Expression.Add(
            Expression.VariableRef("x"),
            Expression.VariableRef("y")
        )
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("リストを解析する")
    fun parseExpression_test011() {
        val result = parseExpression("listOf")
        val expected = Expression.List("Object")
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("変数宣言を解析する")
    fun parseLine_test001() {
        val result = parseLine("var x = 10")
        val expected = Statement.VarDecl("x", Expression.IntLiteral(10))
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("変数代入を解析する")
    fun parseLine_test002() {
        val result = parseLine("x = 5")
        val expected = Statement.VarAssign("x", Expression.IntLiteral(5))
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("Print文を解析する")
    fun parseLine_test003() {
        val result = parseLine("print 15")
        val expected = Statement.Print(Expression.IntLiteral(15))
        assertEquals(expected, result)
    }
}