package net.perfectdreams.loritta.utils.locale

object PseudoLocalization {
    /**
     * Converts the [before] to a string for a pseudolocalization locale, used to test localization
     */
    fun convertWord(before: String): String {
        // http://www.pseudolocalize.com/
        var after = ""

        var isControl = false

        before.forEach { c ->
            // If we replace the stuff inside of the {...} blocks, this can cause issues when trying to format
            // the string (example: {0,number}, number is replaced and this causes an error)
            if (c == '{')
                isControl = true
            if (c == '}')
                isControl = false

            if (!isControl) {
                val out = when (c) {
                    'a' -> 'á'
                    'b' -> 'β'
                    'c' -> 'ç'
                    'd' -> 'δ'
                    'e' -> 'è'
                    'f' -> 'ƒ'
                    'g' -> 'ϱ'
                    'h' -> 'λ'
                    'i' -> 'ï'
                    'j' -> 'J'
                    'k' -> 'ƙ'
                    'l' -> 'ℓ'
                    'm' -> '₥'
                    'n' -> 'ñ'
                    'o' -> 'ô'
                    'p' -> 'ƥ'
                    'q' -> '9'
                    'r' -> 'ř'
                    's' -> 'ƨ'
                    't' -> 'ƭ'
                    'u' -> 'ú'
                    'v' -> 'Ʋ'
                    'w' -> 'ω'
                    'x' -> 'ж'
                    'y' -> '¥'
                    'z' -> 'ƺ'
                    'A' -> 'Â'
                    'B' -> 'ß'
                    'C' -> 'Ç'
                    'D' -> 'Ð'
                    'E' -> 'É'
                    'F' -> 'F'
                    'G' -> 'G'
                    'H' -> 'H'
                    'I' -> 'Ì'
                    'J' -> 'J'
                    'K' -> 'K'
                    'L' -> '£'
                    'M' -> 'M'
                    'N' -> 'N'
                    'O' -> 'Ó'
                    'P' -> 'Þ'
                    'Q' -> 'Q'
                    'R' -> 'R'
                    'S' -> '§'
                    'T' -> 'T'
                    'U' -> 'Û'
                    'V' -> 'V'
                    'W' -> 'W'
                    'X' -> 'X'
                    'Y' -> 'Ý'
                    'Z' -> 'Z'
                    else -> c
                }

                after += out
            } else {
                after += c
            }
        }

        return after
    }
}