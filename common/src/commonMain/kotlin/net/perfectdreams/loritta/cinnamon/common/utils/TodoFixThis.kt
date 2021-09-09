package net.perfectdreams.loritta.cinnamon.common.utils

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.i18nhelper.core.keys.StringI18nKey

/**
 * An i18n key that can be used as a TODO
 *
 * This is useful if you are still planning out your i18n keys and you don't want to write the text yet, however this
 * should *never* be used in production, only for development.
 */
val TodoFixThisKey = StringI18nKey("TODO_FIX_THIS")

/**
 * An i18n key that can be used as a TODO
 *
 * This is useful if you are still planning out your i18n keys and you don't want to write the text yet, however this
 * should *never* be used in production, only for development.
 */
val TodoFixThisData = StringI18nData(TodoFixThisKey, emptyMap())