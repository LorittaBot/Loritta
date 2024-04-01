package net.perfectdreams.loritta.morenitta.utils

import java.time.LocalDateTime
import java.time.Month

object AprilFools {
    fun isAprilFools(): Boolean {
        val date = LocalDateTime.now(Constants.LORITTA_TIMEZONE)
        return date.month == Month.APRIL && date.dayOfMonth == 1
    }
}