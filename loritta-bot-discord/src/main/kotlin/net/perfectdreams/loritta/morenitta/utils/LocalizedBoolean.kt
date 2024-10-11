package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.i18n.I18nKeys

fun Boolean.toLocalized() =
    if (this) I18nKeys.Common.FancyBoolean.True
    else I18nKeys.Common.FancyBoolean.False