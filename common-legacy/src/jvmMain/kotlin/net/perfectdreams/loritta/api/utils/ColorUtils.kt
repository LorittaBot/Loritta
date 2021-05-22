package net.perfectdreams.loritta.utils

import java.awt.Color

object ColorUtils {
    val HEX_PATTERN = "#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})".toPattern()
    val RGB_PATTERN = "(\\d{1,3})(?:,| |, )(\\d{1,3})(?:,| |, )(\\d{1,3})(?:(?:,| |, )(\\d{1,3}))?".toPattern()

    /**
     * Gets color from string
     *
     * @param str argument for searching color
     * @see Color
     *
     * */
    fun getColorFromString(str: String): Color? {
        var color: Color? = null
        val hexMatcher = HEX_PATTERN.matcher(str)
        val rgbMatcher = RGB_PATTERN.matcher(str)

        if (hexMatcher.find()) { // Hexadecimal
            color = Color.decode("#" + hexMatcher.group(1))
        }

        if (rgbMatcher.find()) { // RGB
            var r = rgbMatcher.group(1).toInt()
            var g = rgbMatcher.group(2).toInt()
            var b = rgbMatcher.group(3).toInt()

            color = Color(r, g, b)
        }

        var packedInt = str.toIntOrNull()

        if (packedInt != null) { // Packed Int
            color = Color(packedInt)
        }
        return color
    }
}