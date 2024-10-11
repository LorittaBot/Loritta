package net.perfectdreams.loritta.cinnamon.discord.utils

import net.dv8tion.jda.api.interactions.DiscordLocale

object I18nContextUtils {
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