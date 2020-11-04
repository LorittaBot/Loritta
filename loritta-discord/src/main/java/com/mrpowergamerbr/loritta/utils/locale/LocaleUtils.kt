package com.mrpowergamerbr.loritta.utils.locale

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.utils.Constants

fun BaseLocale.getLocaleId(loritta: Loritta = LorittaLauncher.loritta) =
        loritta.locales.entries.first { it.value == this }.key

fun LegacyBaseLocale.getLocaleId(loritta: Loritta = LorittaLauncher.loritta) =
        loritta.legacyLocales.entries.first { it.value == this }.key

fun Profile.getBaseLocale(loritta: Loritta = LorittaLauncher.loritta, default: LegacyBaseLocale? = null): BaseLocale =
        getLegacyBaseLocale(loritta).toNewLocale()

fun Profile.getLegacyBaseLocale(loritta: Loritta = LorittaLauncher.loritta, default: LegacyBaseLocale? = null): LegacyBaseLocale {
    val settings = loritta.transaction { settings }

    return if (settings.language == null && default != null)
        default
    else loritta.getLegacyLocaleById(settings.language ?: default?.getLocaleId(loritta) ?: Constants.DEFAULT_LOCALE_ID)
}
