package net.perfectdreams.loritta.utils.math

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
            x = java.lang.Double.parseDouble(input.substring(startPos, this.pos))
        } else if (ch >= 'a'.toInt() && ch <= 'z'.toInt()) { // functions
            while (ch >= 'a'.toInt() && ch <= 'z'.toInt()) nextChar()
            val func = input.substring(startPos, this.pos)
            x = parseFactor()
            if (func == "sqrt")
                x = Math.sqrt(x)
            else if (func == "cbrt")
                x = Math.cbrt(x)
            else if (func == "sin")
                x = Math.sin(Math.toRadians(x))
            else if (func == "cos")
                x = Math.cos(Math.toRadians(x))
            else if (func == "tan")
                x = Math.tan(Math.toRadians(x))
            else
                throw RuntimeException("Unknown function: $func")
        } else {
            throw RuntimeException("Unexpected: " + ch.toChar())
        }

        if (eat('^'.toInt())) x = Math.pow(x, parseFactor()) // exponentiation
        if (eat('%'.toInt())) x %= parseFactor() // mod

        return x
    }
}