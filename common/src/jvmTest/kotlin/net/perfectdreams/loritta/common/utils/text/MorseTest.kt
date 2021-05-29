package net.perfectdreams.loritta.common.utils.text

import org.assertj.core.api.Assertions
import org.junit.Test

class MorseTest {
    private val morseInput = "abcdefghijklmnopqrstuvwxyz 1234567890"
    private val morseOutput = ".- -... -.-. -.. . ..-. --. .... .. .--- -.- .-.. -- -. --- .--. --.- .-. ... - ..- ...- .-- -..- -.-- --.. / .---- ..--- ...-- ....- ..... -.... --... ---.. ----. ----- "
    
    @Test
    fun `test conversion to morse`() {
        Assertions.assertThat(
            MorseUtils.toMorse(morseInput)
        ).isEqualTo(morseOutput)
    }

    @Test
    fun `test conversion from morse`() {
        Assertions.assertThat(
            MorseUtils.fromMorse(morseOutput)
        ).isEqualTo(morseInput.toUpperCase())
    }
}