package net.perfectdreams.loritta.utils.locale

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

fun BaseLocale.getLocaleId(loritta: Loritta = LorittaLauncher.loritta) =
        loritta.locales.entries.first { it.value == this }.key

fun Profile.getBaseLocale(loritta: Loritta = LorittaLauncher.loritta, default: BaseLocale? = null): BaseLocale =
        if (settings.language == null && default != null)
            default
        else loritta.getLocaleById(settings.language ?: default?.getLocaleId(loritta) ?: Constants.DEFAULT_LOCALE_ID)