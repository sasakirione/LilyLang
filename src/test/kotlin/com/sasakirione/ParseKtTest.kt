package com.sasakirione

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ParseKtTest {

    @Test
    fun parseExpression_test001() {
        val result = parseExpression("42")
        val expected = Expression.IntLiteral(42)
        assertEquals(expected, result)
    }

    @Test
    fun parseExpression_test002() {
        val result = parseExpression("x")
        val expected = Expression.VariableRef("x")
        assertEquals(expected, result)
    }

    @Test
    fun parseExpression_test003() {
        val result = parseExpression("3 + 2")
        val expected = Expression.Add(
            Expression.IntLiteral(3),
            Expression.IntLiteral(2)
        )
        assertEquals(expected, result)
    }

    @Test
    fun parseExpression_test004() {
        val result = parseExpression("10 - 7")
        val expected = Expression.Sub(
            Expression.IntLiteral(10),
            Expression.IntLiteral(7)
        )
        assertEquals(expected, result)
    }

    @Test
    fun parseExpression_test005() {
        val result = parseExpression("6 * 3")
        val expected = Expression.Mul(
            Expression.IntLiteral(6),
            Expression.IntLiteral(3)
        )
        assertEquals(expected, result)
    }

    @Test
    fun parseExpression_test006() {
        val result = parseExpression("12 / 4")
        val expected = Expression.Div(
            Expression.IntLiteral(12),
            Expression.IntLiteral(4)
        )
        assertEquals(expected, result)
    }

    @Test
    fun parseExpression_test007() {
        val result = parseExpression("15 % 4")
        val expected = Expression.Mod(
            Expression.IntLiteral(15),
            Expression.IntLiteral(4)
        )
        assertEquals(expected, result)
    }

    @Test
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
    fun parseExpression_test010() {
        val result = parseExpression("x + y")
        val expected = Expression.Add(
            Expression.VariableRef("x"),
            Expression.VariableRef("y")
        )
        assertEquals(expected, result)
    }

    @Test
    fun parseExpression_test011() {
        val result = parseExpression("listOf")
        val expected = Expression.List("Object")
        assertEquals(expected, result)
    }
}