package net.perfectdreams.loritta.utils.text

object VaporwaveUtils {
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

            // TODO: Fix (maybe by checking if it is in a valid range by using RegEx?)
            /* if (Character.getType(vaporC) != 2) {
                sb.append(c)
                continue
            } */

            if (isUpperCase)
                vaporC = vaporC.toUpperCase()
            sb.append(vaporC)
        }
        return sb.toString()
    }

    // Yet another case of "Kotlin stdlib does not have this for some reason"
    private fun Char.isUpperCase() = this == this.toUpperCase()
}