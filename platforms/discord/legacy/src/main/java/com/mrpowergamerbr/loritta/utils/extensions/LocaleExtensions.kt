package com.mrpowergamerbr.loritta.utils.extensions

import net.perfectdreams.loritta.common.locale.BaseLocale
import java.util.*

/**
 * Returns the Java Locale (used for dates, etc) for the specified [BaseLocale]
 */
fun BaseLocale.toJavaLocale(): Locale {
    val localeId = this.id

    return Locale(
            when (localeId) {
                "default" -> "pt_BR"
                "pt-pt" -> "pt_PT"
                "en-us" -> "en_US"
                "es-es" -> "es_ES"
                else -> "pt_BR"
            }
    )
}