package net.perfectdreams.loritta.common.utils.text

object MorseUtils {
    private val morseMap = mapOf(
        // ALFABETO
        'A' to ".-",
        'B' to "-...",
        'C' to "-.-.",
        'D' to "-..",
        'E' to ".",
        'F' to "..-.",
        'G' to "--.",
        'H' to "....",
        'I' to "..",
        'J' to ".---",
        'K' to "-.-",
        'L' to ".-..",
        'M' to "--",
        'N' to "-.",
        'O' to "---",
        'P' to ".--.",
        'Q' to "--.-",
        'R' to ".-.",
        'S' to "...",
        'T' to "-",
        'U' to "..-",
        'V' to "...-",
        'W' to ".--",
        'X' to "-..-",
        'Y' to "-.--",
        'Z' to "--..",

        // NÚMEROS
        '1' to ".----",
        '2' to "..---",
        '3' to "...--",
        '4' to "....-",
        '5' to ".....",
        '6' to "-....",
        '7' to "--...",
        '8' to "---..",
        '9' to "----.",
        '0' to "-----",

        // PONTUAÇÕES COMUNS
        '.' to ".-.-.-",
        ',' to "--..--",
        '?' to "..--..",
        '\'' to ".----.",
        '!' to "-.-.--",
        '/' to "-..-.",
        '(' to "-.--.",
        ')' to "-.--.-",
        '&' to ".-...",
        ':' to "---...",
        ';' to "-.-.-.",
        '=' to "-...-",
        '-' to "-....-",
        '_' to "..--.-",
        '"' to ".-..-.",
        '$' to "...-..-",
        '@' to ".--.-.",
        ' ' to "/",

        // OUTROS CARACTERES
        'ä' to ".-.-",
        'à' to ".--.-",
        'ç' to "-.-..",
        'ð' to "..--.",
        'è' to ".-..-",
        'é' to "..-..",
        'ĝ' to "--.-.",
        'ĥ' to "-.--.",
        'ĵ' to ".---.",
        'ñ' to "--.--",
        'ö' to "---.",
        'ŝ' to "...-.",
        'þ' to ".--..",
        'ü' to "..--"
    )

    fun fromMorse(input: String): FromMorseConversionResult {
        val unknownMorseCodes = mutableSetOf<String>()

        // Criar uma string vazia para guardar a nossa mensagem em texto comum
        val result = buildString {
            // Separar nossa string em morse em espaços para fazer um for nela
            input.split(" ").forEach { inMorse ->
                // Pegar o valor do char em morse
                val inTextEntry = morseMap.entries.firstOrNull { it.value == inMorse }

                if (inTextEntry != null) { // E, caso seja diferente de null...
                    append(inTextEntry.key) // Pegar o nosso valor e colocar na nossa string!
                } else {
                    unknownMorseCodes.add(inMorse) // If it doesn't exist, add to our set
                }
            }
        }.trim()

        return if (result.isEmpty())
            InvalidFromMorseConversionResult
        else
            ValidFromMorseConversionResult(result, unknownMorseCodes)
    }

    fun toMorse(input: String): ToMorseConversionResult {
        val upper = input.uppercase()
        val unknownCharacters = mutableSetOf<Char>()

        // Criar uma string vazia para guardar a nossa mensagem em morse
        val result = buildString {
            // Fazer um for na nossa mensagem
            upper.toCharArray().forEach { char ->
                // Pegar o valor do char em morse
                val inMorse = morseMap[char]

                if (inMorse != null) { // E, caso seja diferente de null...
                    append("$inMorse ") // Pegar o nosso valor e colocar na nossa string!
                } else {
                    unknownCharacters.add(char) // If it doesn't exist, add to our set
                }
            }
        }.trim()

        return if (result.isEmpty())
            InvalidToMorseConversionResult
        else
            ValidToMorseConversionResult(result, unknownCharacters)
    }

    sealed class FromMorseConversionResult
    class ValidFromMorseConversionResult(
        val text: String,
        val unknownMorseCodes: Set<String>
    ) : FromMorseConversionResult()
    object InvalidFromMorseConversionResult : FromMorseConversionResult()

    sealed class ToMorseConversionResult
    class ValidToMorseConversionResult(
        val morse: String,
        val unknownCharacters: Set<Char>
    ) : ToMorseConversionResult()
    object InvalidToMorseConversionResult : ToMorseConversionResult()
}