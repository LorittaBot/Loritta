package net.perfectdreams.loritta.common.utils.math

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class MathParser(val input: String) {
    var pos = -1
    var ch: Int = 0

    fun nextChar() {
        ch = if (++pos < input.length) input[pos].toInt() else -1
    }

    fun eat(charToEat: Int): Boolean {
        while (ch == ' '.toInt()) nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    fun parse(): Double {
        nextChar()
        val x = parseExpression()
        if (pos < input.length) throw RuntimeException("Unexpected: " + ch.toChar())
        return x
    }

    // Grammar:
    // expression = term | expression `+` term | expression `-` term
    // term = factor | term `*` factor | term `/` factor
    // factor = `+` factor | `-` factor | `(` expression `)`
    //        | number | functionName factor | factor `^` factor
    fun parseExpression(): Double {
        var x = parseTerm()
        while (true) {
            if (eat('+'.toInt()))
                x += parseTerm() // addition
            else if (eat('-'.toInt()))
                x -= parseTerm() // subtraction
            else
                return x
        }
    }

    fun parseTerm(): Double {
        var x = parseFactor()
        while (true) {
            if (eat('*'.toInt()))
                x *= parseFactor() // multiplication
            else if (eat('/'.toInt()))
                x /= parseFactor() // division
            else if (eat('%'.toInt())) // mod
                x %= parseFactor()
            else
                return x
        }
    }

    fun parseFactor(): Double {
        if (eat('+'.toInt())) return parseFactor() // unary plus
        if (eat('-'.toInt())) return -parseFactor() // unary minus

        var x: Double
        val startPos = this.pos
        if (eat('('.toInt())) { // parentheses
            x = parseExpression()
            eat(')'.toInt())
        } else if (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) { // numbers
            while (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) nextChar()
            x = input.substring(startPos, this.pos).toDouble()
        } else if (ch >= 'a'.toInt() && ch <= 'z'.toInt()) { // functions
            while (ch >= 'a'.toInt() && ch <= 'z'.toInt()) nextChar()
            val func = input.substring(startPos, this.pos)
            x = parseFactor()
            x = when (func) {
                "sqrt" -> sqrt(x)
                // https://stackoverflow.com/a/55481868/7271796
                "cbrt" -> x.pow(1/3.toDouble())
                "sin" -> sin(MathUtils.toRadians(x))
                "cos" -> cos(MathUtils.toRadians(x))
                "tan" -> tan(MathUtils.toRadians(x))
                else -> throw RuntimeException("Unknown function: $func")
            }
        } else {
            throw RuntimeException("Unexpected: " + ch.toChar())
        }

        if (eat('^'.toInt())) x = x.pow(parseFactor()) // exponentiation
        if (eat('%'.toInt())) x %= parseFactor() // mod

        return x
    }
}