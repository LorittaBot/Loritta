package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.utils.TimeUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TimeUtilsTest {
    @Test
    fun `check time time and date`() {
        val localDateTime = TimeUtils.convertToLocalDateTimeRelativeToNow("23:07 08/11/2020")

        assertThat(localDateTime.toString()).isEqualTo("2020-11-08T23:07-03:00[America/Sao_Paulo]")
    }

    @Test
    fun `check time date and time`() {
        val localDateTime = TimeUtils.convertToLocalDateTimeRelativeToNow("23:07 08/11/2020")

        assertThat(localDateTime.toString()).isEqualTo("2020-11-08T23:07-03:00[America/Sao_Paulo]")
    }
}