package net.perfectdreams.loritta.common.utils.math

import net.perfectdreams.loritta.common.utils.math.MathUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class MathEvalTest {
    @Test
    fun `test 2 + 2`() {
        assertThat(MathUtils.evaluate("2 + 2"))
            .isEqualTo(4.0)
    }

    @Test
    fun `test 2 x 2`() {
        assertThat(MathUtils.evaluate("2 * 2"))
            .isEqualTo(4.0)
    }

    @Test
    fun `test 2 - 2`() {
        assertThat(MathUtils.evaluate("2 - 2"))
            .isEqualTo(0.0)
    }

    @Test
    fun `test 2 div 2`() {
        assertThat(MathUtils.evaluate("2 / 2"))
            .isEqualTo(1.0)
    }

    @Test
    fun `test 16 mod 10`() {
        assertThat(MathUtils.evaluate("16 % 10"))
            .isEqualTo(6.0)
    }

    @Test
    fun `test sqrt 1000`() {
        assertThat(MathUtils.evaluate("sqrt 1000"))
            .isEqualTo(31.622776601683793)
    }

    @Test
    fun `test complex expression`() {
        assertThat(MathUtils.evaluate("4002 * ((10 * 10) + (1337 / 10) - (5555 - 5000))"))
            .isEqualTo(-1285842.6)
    }

    @Test
    fun `test invalid evalutation ends with expression`() {
        assertThatThrownBy {
            MathUtils.evaluate("lori is cute 123 / 100")
        }.isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun `test invalid evalutation starts with expression`() {
        assertThatThrownBy {
            MathUtils.evaluate("123 / 100 lori is cute")
        }.isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun `test invalid evalutation no expression`() {
        assertThatThrownBy {
            MathUtils.evaluate("lori is cute")
        }.isInstanceOf(RuntimeException::class.java)
    }
}