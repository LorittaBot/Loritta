package net.perfectdreams.loritta.cinnamon.discord.utils

import net.perfectdreams.i18nhelper.core.I18nContext
import java.text.NumberFormat
import java.text.ParseException
import java.util.*

object NumberUtils {
    /**
     * Converts a shortened [String] number (1k, 1.5k, 1M, 2.3kk, etc) to a [Long] number
     *
     * This also converts a normal number (non shortened) to a [Long]
     *
     * @param i18nContext the I18nContext, used to parse numbers in the user's preferred number format
     * @param input       the shortened number
     * @return            the number as long or null if it is a non valid (example: text) number
     */
    fun convertShortenedNumberToLong(i18nContext: I18nContext, input: String) = convertShortenedNumberToLong(Locale.forLanguageTag(i18nContext.language.info.formattingLanguageId), input)

    /**
     * Converts a shortened [String] number (1k, 1.5k, 1M, 2.3kk, etc) to a [Long] number
     *
     * This also converts a normal number (non shortened) to a [Long]
     *
     * @param i18nContext the I18nContext, used to parse numbers in the user's preferred number format
     * @param input       the shortened number
     * @return            the number as long or null if it is a non valid (example: text) number
     */
    fun convertShortenedNumberToLong(locale: Locale, input: String) = convertShortenedNumberToLong(NumberFormat.getNumberInstance(locale), input)

    /**
     * Converts a shortened [String] number (1k, 1.5k, 1M, 2.3kk, etc) to a [Long] number
     *
     * This also converts a normal number (non shortened) to a [Long]
     *
     * @param i18nContext the I18nContext, used to parse numbers in the user's preferred number format
     * @param input       the shortened number
     * @return            the number as long or null if it is a non valid (example: text) number
     */
    fun convertShortenedNumberToLong(numberFormat: NumberFormat, input: String): Long? {
        val inputAsLowerCase = input.lowercase()

        try {
            return when {
                inputAsLowerCase.endsWith("m") -> parseStringToDoubleOrNull(
                    numberFormat,
                    inputAsLowerCase.removeSuffix("m")
                )?.times(1_000_000)?.toLong()
                inputAsLowerCase.endsWith("kk") -> parseStringToDoubleOrNull(
                    numberFormat,
                    inputAsLowerCase.removeSuffix("kk")
                )?.times(1_000_000)?.toLong()
                inputAsLowerCase.endsWith("k") -> parseStringToDoubleOrNull(
                    numberFormat,
                    inputAsLowerCase.removeSuffix("k")
                )?.times(1_000)
                    ?.toLong()
                else -> parseStringToLongOrNull(numberFormat, input)
            }
        } catch (e: ParseException) {
            return null
        }
    }

    private fun parseStringToDoubleOrNull(numberFormat: NumberFormat, input: String) = try {
        numberFormat.parse(input).toDouble()
    } catch (e: ParseException) {
        null
    }

    private fun parseStringToLongOrNull(numberFormat: NumberFormat, input: String) = try {
        numberFormat.parse(input).toLong()
    } catch (e: ParseException) {
        null
    }
}