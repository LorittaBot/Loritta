package net.perfectdreams.loritta.utils

import java.time.format.DateTimeFormatter
import java.util.*

object DateUtils {
    val PRETTY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            .withLocale(Locale.US)
            .withZone(TimeZone.getTimeZone("GMT").toZoneId())
    val PRETTY_FILE_SAFE_UNDERSCORE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")
            .withLocale(Locale.US)
            .withZone(TimeZone.getTimeZone("GMT").toZoneId())
}