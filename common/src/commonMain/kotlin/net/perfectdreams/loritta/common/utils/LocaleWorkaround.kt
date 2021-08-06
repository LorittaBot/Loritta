package net.perfectdreams.loritta.common.utils

import net.perfectdreams.i18nhelper.core.keydata.StringTranslationData
import net.perfectdreams.i18nhelper.core.keys.StringTranslationKey
import net.perfectdreams.loritta.common.locale.LocaleKeyData

// TODO: Workaround, please remove later!
fun LocaleKeyData.toI18nHelper() = StringTranslationData(StringTranslationKey(this.key), mapOf())