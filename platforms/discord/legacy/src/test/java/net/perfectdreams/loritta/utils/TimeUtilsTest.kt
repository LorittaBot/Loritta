package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.utils.TimeUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class TimeUtilsTest {
    @Test
    fun `check time and date`() {
        val localDateTime = TimeUtils.convertToLocalDateTimeRelativeToTime(
                "23:07 08/11/2020",
                LocalDateTime.of(2020, 3,30, 12, 0, 0, 0)
                        .atZone(TimeUtils.TIME_ZONE)
        )

        assertThat(localDateTime.toString()).isEqualTo("2020-11-08T23:07-03:00[America/Sao_Paulo]")
    }

    @Test
    fun `check date and time`() {
        val localDateTime = TimeUtils.convertToLocalDateTimeRelativeToTime(
                "08/11/2020 23:07",
                LocalDateTime.of(2020, 3,30, 12, 0, 0, 0)
                        .atZone(TimeUtils.TIME_ZONE)
        )

        assertThat(localDateTime.toString()).isEqualTo("2020-11-08T23:07-03:00[America/Sao_Paulo]")
    }
}