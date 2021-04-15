package net.perfectdreams.loritta.common.utils.text

object VaporwaveUtils {
    // From FF01 to FF65
    private val FULL_WIDTH_CHARACTERS_RANGE = 65281..65381

    /**
     * Converte um texto para full width
     *
     * @return O texto em formato full width
     */
    fun vaporwave(str: String): String {
        val sb = StringBuilder()
        for (_c in str.toCharArray()) {
            val isUpperCase = _c.isUpperCase()
            val c = _c.toLowerCase()
            if (c.isWhitespace()) {
                sb.append(" ")
                continue
            }
            var vaporC = (c.toInt() + 0xFEE0).toChar()


            if (vaporC.toInt() !in 65281..65381) {
                // If it isn't within our range, let's append the original character
                sb.append(c)
                continue
            }

            if (isUpperCase)
                vaporC = vaporC.toUpperCase()
            sb.append(vaporC)
        }
        return sb.toString()
    }

    // Yet another case of "Kotlin stdlib does not have this for some reason"
    private fun Char.isUpperCase() = this == this.toUpperCase()
}