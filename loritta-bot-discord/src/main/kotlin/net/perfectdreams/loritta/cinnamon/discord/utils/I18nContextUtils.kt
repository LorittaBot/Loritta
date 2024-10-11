package net.perfectdreams.loritta.cinnamon.discord.utils

import net.dv8tion.jda.api.interactions.DiscordLocale
import net.perfectdreams.loritta.common.locale.LanguageManager

object I18nContextUtils {
    /**
     * Converts a [DiscordLocale] to a i18nContext, if there isn't a matched language, returns null
     *
     * This should be updated every time a new language is introduced to Loritta
     *
     * @param languageManager the language manager
     * @param jdaLocale       the discord locale
     * @return                the [I18nContext] or, if it is not present, null
     */
    fun convertDiscordLocaleToI18nContext(languageManager: LanguageManager, jdaLocale: DiscordLocale) = when (jdaLocale) {
        DiscordLocale.ENGLISH_US -> languageManager.getI18nContextById("en")
        DiscordLocale.ENGLISH_UK -> languageManager.getI18nContextById("en")
        DiscordLocale.PORTUGUESE_BRAZILIAN -> languageManager.getI18nContextById("pt")
        else -> null
    }

    /**
     * Converts a [languageId] (example: "pt") to a JDA [DiscordLocale]
     *
     * This should be updated every time a new language is introduced to Loritta
     *
     * @param languageId the language ID
     * @return           the [Locale] or, if it is not present, null
     */
    fun convertLanguageIdToJDALocale(languageId: String) = when (languageId) {
        "en" -> DiscordLocale.ENGLISH_US
        "pt" -> DiscordLocale.PORTUGUESE_BRAZILIAN
        else -> null
    }
}