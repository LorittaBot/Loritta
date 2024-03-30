package net.perfectdreams.loritta.cinnamon.discord.utils.google

import net.dv8tion.jda.api.interactions.DiscordLocale

object GoogleAPIUtils {
    val DISCORD_LOCALE_TO_LANGUAGE_MAP = DiscordLocale.values().map {
        it to when (it) {
            DiscordLocale.PORTUGUESE_BRAZILIAN -> GoogleVisionLanguage.PORTUGUESE
            DiscordLocale.BULGARIAN -> GoogleVisionLanguage.BULGARIAN
            DiscordLocale.CHINESE_CHINA -> GoogleVisionLanguage.SIMPLIFIED_CHINESE
            DiscordLocale.CHINESE_TAIWAN -> GoogleVisionLanguage.TRADITIONAL_CHINESE
            DiscordLocale.CROATIAN -> GoogleVisionLanguage.CROATIAN
            DiscordLocale.CZECH -> GoogleVisionLanguage.CZECH
            DiscordLocale.DANISH -> GoogleVisionLanguage.DANISH
            DiscordLocale.DUTCH -> GoogleVisionLanguage.DUTCH
            DiscordLocale.FINNISH -> GoogleVisionLanguage.FINNISH
            DiscordLocale.FRENCH -> GoogleVisionLanguage.FRENCH
            DiscordLocale.GERMAN -> GoogleVisionLanguage.GERMAN
            DiscordLocale.GREEK -> GoogleVisionLanguage.GREEK
            DiscordLocale.HINDI -> GoogleVisionLanguage.HINDI
            DiscordLocale.HUNGARIAN -> GoogleVisionLanguage.HUNGARIAN
            DiscordLocale.ITALIAN -> GoogleVisionLanguage.ITALIAN
            DiscordLocale.JAPANESE -> GoogleVisionLanguage.JAPANESE
            DiscordLocale.KOREAN -> GoogleVisionLanguage.KOREAN
            DiscordLocale.LITHUANIAN -> GoogleVisionLanguage.LITHUANIAN
            DiscordLocale.NORWEGIAN -> GoogleVisionLanguage.NORWEGIAN
            DiscordLocale.POLISH -> GoogleVisionLanguage.POLISH
            DiscordLocale.ROMANIAN_ROMANIA -> GoogleVisionLanguage.ROMANIAN
            DiscordLocale.RUSSIAN -> GoogleVisionLanguage.RUSSIAN
            DiscordLocale.SPANISH -> GoogleVisionLanguage.SPANISH
            DiscordLocale.SWEDISH -> GoogleVisionLanguage.SWEDISH
            DiscordLocale.THAI -> GoogleVisionLanguage.THAI
            DiscordLocale.TURKISH -> GoogleVisionLanguage.TURKISH
            DiscordLocale.UKRAINIAN -> GoogleVisionLanguage.UKRAINIAN
            DiscordLocale.VIETNAMESE -> GoogleVisionLanguage.VIETNAMESE
            DiscordLocale.ENGLISH_UK -> GoogleVisionLanguage.ENGLISH
            else -> GoogleVisionLanguage.ENGLISH
        }
    }.toMap()

    // This is a bit of a hack
    fun fromVisionLanguageToTranslateLanguage(visionLanguage: GoogleVisionLanguage) = GoogleTranslateLanguage.valueOf(visionLanguage.name)
}