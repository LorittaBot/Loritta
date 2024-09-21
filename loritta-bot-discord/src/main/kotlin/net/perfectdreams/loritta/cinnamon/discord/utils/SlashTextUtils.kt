package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.kord.common.Locale
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.common.commands.CommandCategory

/**
 * Miscellaneous utilities for Application Commands' text
 */
object SlashTextUtils {
    fun shorten(languageManager: LanguageManager, i18nData: StringI18nData) = shorten(languageManager.getI18nContextById("en").get(i18nData))

    fun shorten(string: String) = string.shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)

    fun shortenAll(descriptionLocalizations: Map<Locale, String>?) = descriptionLocalizations?.let {
        it.mapValues {
            shorten(it.value)
        }
    }

    fun buildDescription(i18nContext: I18nContext, description: StringI18nData, category: CommandCategory) = buildString {
        // It looks like this
        // "「Emoji Category」 Description"
        append("「")
        // Before we had unicode emojis reflecting each category, but the emojis look super ugly on Windows 10
        // https://cdn.discordapp.com/attachments/297732013006389252/973613713456250910/unknown.png
        // So we removed it ;)
        append(category.getLocalizedName(i18nContext))
        append("」")
        // Looks better without this whitespace
        // append(" ")
//        append(i18nContext.get(description))
    }.shortenWithEllipsis(DiscordResourceLimits.Command.Description.Length)

    /**
     * Creates a map containing all translated strings of [i18nKey], excluding the [defaultLocale].
     *
     * @param defaultLocale the default locale used when creating the slash commands, this won't be present in the map.
     * @param i18nKey the key
     */
    fun createLocalizedStringMapExcludingDefaultLocale(languageManager: LanguageManager, key: StringI18nData) = mutableMapOf<Locale, String>().apply {
        val defaultI18nContext = languageManager.getI18nContextById("en")

        // We will ignore the default i18nContext because that would be redundant
        for ((languageId, i18nContext) in languageManager.languageContexts.filter { it.value != defaultI18nContext }) {
            if (i18nContext.language.textBundle.strings.containsKey(key.key.key) && i18nContext.language.textBundle.strings[key.key.key] != defaultI18nContext.language.textBundle.strings[key.key.key]) {
                val kordLocale = I18nContextUtils.convertLanguageIdToKordLocale(languageId)
                if (kordLocale != null)
                    this[kordLocale] = i18nContext.get(key)
            }
        }
    }

    /**
     * Creates a map containing all translated strings of [i18nKey], excluding the [defaultLocale].
     *
     * @param defaultLocale the default locale used when creating the slash commands, this won't be present in the map.
     * @param i18nKey the key
     */
    fun createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager: LanguageManager, key: StringI18nData)
            = shortenAll(createLocalizedStringMapExcludingDefaultLocale(languageManager, key))

    /**
     * Creates a map containing all translated strings of [i18nKey], excluding the [defaultLocale].
     *
     * @param defaultLocale the default locale used when creating the slash commands, this won't be present in the map.
     * @param i18nKey the key
     */
    fun createLocalizedDescriptionMapExcludingDefaultLocale(languageManager: LanguageManager, description: StringI18nData, category: CommandCategory) = mutableMapOf<Locale, String>().apply {
        val defaultI18nContext = languageManager.getI18nContextById("en")

        // We will ignore the default i18nContext because that would be redundant
        for ((languageId, i18nContext) in languageManager.languageContexts.filter { it.value != defaultI18nContext }) {
            if (i18nContext.language.textBundle.strings.containsKey(description.key.key) && i18nContext.language.textBundle.strings[description.key.key] != defaultI18nContext.language.textBundle.strings[description.key.key]) {
                val kordLocale = I18nContextUtils.convertLanguageIdToKordLocale(languageId)
                if (kordLocale != null)
                    this[kordLocale] = buildDescription(i18nContext, description, category)
            }
        }
    }

    /**
     * Creates a map containing all translated strings of [i18nKey], excluding the [defaultLocale], and automatically shortens it to fit.
     *
     * @param defaultLocale the default locale used when creating the slash commands, this won't be present in the map.
     * @param i18nKey the key
     */
    fun createShortenedLocalizedDescriptionMapExcludingDefaultLocale(languageManager: LanguageManager, description: StringI18nData, category: CommandCategory)
            = shortenAll(createLocalizedDescriptionMapExcludingDefaultLocale(languageManager, description, category))
}