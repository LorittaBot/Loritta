package net.perfectdreams.loritta.common.utils.math

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger

object MathUtils {
    /**
     * Evaluates [input] as a Math expression and returns the result
     *
     * Example: "2 + 2" will return 4
     *
     * @param  input             the input
     * @throws RuntimeException if the input is malformed
     * @return the result
     */
    fun evaluate(input: String) = MathParser(input).parse()

    /**
     * Evaluates [input] as a Math expression and returns the result, if the input is malformed, null will be returned.
     *
     * Example: "2 + 2" will return 4
     *
     * @see evaluate
     * @param  input             the input
     * @return the result
     */
    fun evaluateOrNull(input: String) = try { MathParser(input).parse() } catch (e: RuntimeException) { null }

    private const val DEGREES_TO_RADIANS = 0.017453292519943295

    // Kotlin's stdlib does not have a "toRadians" method
    // Copied from Java's stdlib
    fun toRadians(angdeg: Double): Double {
        return angdeg * DEGREES_TO_RADIANS
    }

    // https://stackoverflow.com/a/7879559/7271796
    fun factorial(number: BigInteger): BigInteger {
        var result = 1.toBigInteger()
        for (factor in 2..number.longValue()) {
            result = result.multiply(factor.toBigInteger())
        }
        return result
    }

    /**
     * Truncates a double to two decimal places
     */
    fun truncateToTwoDecimalPlaces(number: Double): Double {
        return (number * 100).toInt() / 100.0
    }
}