package net.perfectdreams.loritta.cinnamon.utils

import net.perfectdreams.loritta.cinnamon.utils.math.Dice
import org.assertj.core.api.Assertions
import org.junit.Test

class DiceTest {
    @Test
    fun `parse dice with six sides`() {
        val dice = Dice.parse("6")
        validateDices(dice, 1, 1, 6)
    }

    @Test
    fun `parse dice with twelve sides`() {
        val dice = Dice.parse("12")
        validateDices(dice, 1, 1, 12)
    }

    @Test
    fun `parse dice with twenty four sides`() {
        val dice = Dice.parse("24")
        validateDices(dice, 1, 1, 24)
    }

    @Test
    fun `parse two dices with twenty sides`() {
        val dice = Dice.parse("2d20")
        validateDices(dice, 2, 1, 20)
    }

    @Test
    fun `parse three dices with five sides`() {
        val dice = Dice.parse("3d5")
        validateDices(dice, 3, 1, 5)
    }

    @Test
    fun `parse four dices with ten sides`() {
        val dice = Dice.parse("4d10")
        validateDices(dice, 4, 1, 10)
    }

    @Test
    fun `parse dice with values from five to ten`() {
        val dice = Dice.parse("5..10")
        validateDices(dice, 1, 5, 10)
    }

    @Test
    fun `parse ten dices with values from ten to 999`() {
        val dice = Dice.parse("10d100..999")
        validateDices(dice, 10, 100, 999)
    }

    @Test
    fun `parse invalid range dice`() {
        val thrown = Assertions.catchThrowable { Dice.parse("6..a") }
        Assertions.assertThat(thrown).isInstanceOf(Exception::class.java)
    }

    @Test
    fun `parse lower bound higher than upper bound dice`() {
        val thrown = Assertions.catchThrowable { Dice.parse("12..6") }
        Assertions.assertThat(thrown).isInstanceOf(Dice.Companion.LowerBoundHigherThanUpperBoundException::class.java)
    }

    @Test
    fun `dice parser quantity limit`() {
        val thrown = Assertions.catchThrowable { Dice.parse("999d3", 100) }
        Assertions.assertThat(thrown).isInstanceOf(Dice.Companion.TooManyDicesException::class.java)
    }

    private fun validateDices(dices: List<Dice>, quantity: Int, lowerBound: Long, upperBound: Long) {
        Assertions.assertThat(dices.size).isEqualTo(quantity)

        for (dice in dices) {
            Assertions.assertThat(dice.lowerBound).isEqualTo(lowerBound)
            Assertions.assertThat(dice.upperBound).isEqualTo(upperBound)
        }
    }
}