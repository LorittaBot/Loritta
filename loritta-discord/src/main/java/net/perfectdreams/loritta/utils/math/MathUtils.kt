package net.perfectdreams.loritta.utils.math

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
}