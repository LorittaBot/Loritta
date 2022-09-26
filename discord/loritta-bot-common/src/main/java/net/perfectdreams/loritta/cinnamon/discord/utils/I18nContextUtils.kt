package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.kord.common.Locale
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.locale.LanguageManager

object I18nContextUtils {
    /**
     * Creates a map containing all translated strings of [i18nKey], excluding the [defaultLocale].
     *
     * @param defaultLocale the default locale used when creating the slash commands, this won't be present in the map.
     * @param i18nKey the key
     */
    fun createLocalizedStringMapExcludingDefaultLocale(languageManager: LanguageManager, defaultLocale: I18nContext, i18nKey: StringI18nData) = mutableMapOf<Locale, String>().apply {
        // We will ignore the default i18nContext because that would be redundant
        for ((languageId, i18nContext) in languageManager.languageContexts.filter { it.value != defaultLocale }) {
            if (i18nContext.language.textBundle.strings.containsKey(i18nKey.key.key)) {
                val kordLocale = convertLanguageIdToKordLocale(languageId)
                if (kordLocale != null)
                    this[kordLocale] = i18nContext.get(i18nKey)
            }
        }
    }

    /**
     * Converts a [languageId] (example: "pt") to a Kord [Locale]
     *
     * This should be updated every time a new language is introduced to Loritta
     *
     * @param languageId the language ID
     * @return           the [Locale] or, if it is not present, null
     */
    fun convertLanguageIdToKordLocale(languageId: String) = when (languageId) {
        "en" -> Locale.ENGLISH_UNITED_STATES
        "pt" -> Locale.PORTUGUESE_BRAZIL
        else -> null
    }
}