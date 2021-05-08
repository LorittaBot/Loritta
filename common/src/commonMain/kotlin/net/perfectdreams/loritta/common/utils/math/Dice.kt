package net.perfectdreams.loritta.common.utils.math

class Dice(
    val lowerBound: Long,
    val upperBound: Long
) {
    companion object {
        fun parse(expression: String, diceLimit: Int? = null): List<Dice> {
            val dices = mutableListOf<Dice>()

            // Example: 10d5
            // We will drop until the character isn't between 0..9, so the result will be
            // d5
            if (expression.dropWhile { it in '0'..'9' }.startsWith("d")) {
                // So yes, we do have a *extended* dice!
                val quantityOfDices = expression.takeWhile { it != 'd' }.toInt()
                val sidesOfDicesAsString = expression.dropWhile { it != 'd' }.drop(1)

                val lowerBound: Long
                val upperBound: Long

                if (sidesOfDicesAsString.isEmpty()) {
                    lowerBound = 1
                    upperBound = 6
                } else {
                    if (sidesOfDicesAsString.contains("..")) {
                        val pair = parseRange(sidesOfDicesAsString)
                        lowerBound = pair.first
                        upperBound = pair.second
                    } else {
                        lowerBound = 1
                        upperBound = sidesOfDicesAsString.toLong()
                    }
                }

                if (diceLimit != null && quantityOfDices !in 1..diceLimit)
                    throw TooManyDicesException()

                repeat(quantityOfDices) {
                    dices.add(
                        Dice(
                            lowerBound,
                            upperBound
                        )
                    )
                }
            } else {
                val lowerBound: Long
                val upperBound: Long

                // This is the default parsing method, no special "more than one dice" stuff
                // So we will only parse range and stuff
                if (expression.contains("..")) {
                    val pair = parseRange(expression)
                    lowerBound = pair.first
                    upperBound = pair.second
                } else {
                    lowerBound = 1
                    upperBound = expression.toLong()
                }

                dices.add(
                    Dice(lowerBound, upperBound)
                )
            }

            return dices
        }

        private fun parseRange(range: String): Pair<Long, Long> {
            if (!range.contains(".."))
                throw IllegalArgumentException("Not a valid range!")

            val split = range.split("..")
            return Pair(split[0].toLong(), split[1].toLong())
        }

        class TooManyDicesException : IllegalArgumentException()
        class LowerBoundHigherThanUpperBoundException : IllegalArgumentException()
    }

    init {
        if (lowerBound > upperBound)
            throw LowerBoundHigherThanUpperBoundException()
    }
}