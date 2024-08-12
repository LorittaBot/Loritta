package net.perfectdreams.loritta.morenitta.utils

object NumberUtils {
    /**
     * Converts a shortened [String] number (1k, 1.5k, 1M, 2.3kk, etc) to a [Long] number
     *
     * This also converts a normal number (non shortened) to a [Long]
     *
     * @param input the shortened number
     * @return      the number as long or null if it is a non valid (example: text) number
     * @see convertShortenedNumberOrUserSonhosSpecificToLong
     */
    fun convertShortenedNumberToLong(input: String): Long? {
        val inputAsLowerCase = input.toLowerCase()

        return when {
            inputAsLowerCase.endsWith("m") -> inputAsLowerCase.removeSuffix("m").toDoubleOrNull()?.times(1_000_000)?.toLong()
            inputAsLowerCase.endsWith("kk") -> inputAsLowerCase.removeSuffix("kk").toDoubleOrNull()?.times(1_000_000)?.toLong()
            inputAsLowerCase.endsWith("k") -> inputAsLowerCase.removeSuffix("k").toDoubleOrNull()?.times(1_000)?.toLong()
            else -> inputAsLowerCase.toLongOrNull()
        }
    }

    /**
     * Converts a shortened [String] number (1k, 1.5k, 1M, 2.3kk, half, all, etc) to a [Long] number
     *
     * This also converts a normal number (non shortened) to a [Long]
     *
     * The difference between [convertShortenedNumberToLong] and this one is that this one supports values like "half", which returns half of the [userSonhos]
     *
     * @param input the shortened number
     * @return      the number as long or null if it is a non valid (example: text) number
     * @see convertShortenedNumberToLong
     */
    fun convertShortenedNumberOrUserSonhosSpecificToLong(input: String, userSonhos: Long): Long? {
        val fullNumber = convertShortenedNumberToLong(input)
        if (fullNumber != null)
            return fullNumber

        // Additional ways
        return when (input.lowercase()) {
            "half", "metade", "50%" -> {
                userSonhos / 2
            }

            "full", "all", "tudo", "100%" -> {
                userSonhos
            }

            // No matches!
            else -> null
        }
    }
}