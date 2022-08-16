package net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options

import dev.kord.common.Locale
import dev.kord.common.entity.ChannelType
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.Channel
import net.perfectdreams.discordinteraktions.common.autocomplete.AutocompleteHandler
import net.perfectdreams.discordinteraktions.common.commands.options.*
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.SlashTextUtils

abstract class LocalizedCommandOption<T>(
    val languageManager: LanguageManager,
    val descriptionI18n: StringI18nData,
) : NameableCommandOption<T> {
    override val description = languageManager.defaultI18nContext.get(descriptionI18n).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)
    override val descriptionLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, descriptionI18n)
    override val nameLocalizations: Map<Locale, String> = emptyMap()
}

// ===[ STRING ]===
class LocalizedStringCommandOption(
    languageManager: LanguageManager,
    override val name: String,
    descriptionI18n: StringI18nData,
    override val required: Boolean,
    override val choices: List<CommandChoice<String>>?,
    override val minLength: Int?,
    override val maxLength: Int?,
    override val autocompleteExecutor: AutocompleteHandler<String>?
) : LocalizedCommandOption<String>(languageManager, descriptionI18n), StringCommandOption

// ===[ INTEGER ]===
class LocalizedIntegerCommandOption(
    languageManager: LanguageManager,
    override val name: String,
    descriptionI18n: StringI18nData,
    override val required: Boolean,
    override val choices: List<CommandChoice<Long>>?,
    override val minValue: Long?,
    override val maxValue: Long?,
    override val autocompleteExecutor: AutocompleteHandler<Long>?
) : LocalizedCommandOption<Long>(languageManager, descriptionI18n), IntegerCommandOption

// ===[ NUMBER ]===
class LocalizedNumberCommandOption(
    languageManager: LanguageManager,
    override val name: String,
    descriptionI18n: StringI18nData,
    override val required: Boolean,
    override val choices: List<CommandChoice<Double>>?,
    override val minValue: Double?,
    override val maxValue: Double?,
    override val autocompleteExecutor: AutocompleteHandler<Double>?
) : LocalizedCommandOption<Double>(languageManager, descriptionI18n), NumberCommandOption

// ===[ BOOLEAN ]===
class LocalizedBooleanCommandOption(
    languageManager: LanguageManager,
    override val name: String,
    descriptionI18n: StringI18nData,
    override val required: Boolean
) : LocalizedCommandOption<Boolean>(languageManager, descriptionI18n), BooleanCommandOption

// ===[ USER ]===
class LocalizedUserCommandOption(
    languageManager: LanguageManager,
    override val name: String,
    descriptionI18n: StringI18nData,
    override val required: Boolean
) : LocalizedCommandOption<User>(languageManager, descriptionI18n), UserCommandOption

// ===[ CHANNEL ]===
class LocalizedChannelCommandOption(
    languageManager: LanguageManager,
    override val name: String,
    descriptionI18n: StringI18nData,
    override val required: Boolean,
    override val channelTypes: List<ChannelType>?
) : LocalizedCommandOption<Channel>(languageManager, descriptionI18n), ChannelCommandOption

// ===[ ROLE ]===
class LocalizedRoleCommandOption(
    languageManager: LanguageManager,
    override val name: String,
    descriptionI18n: StringI18nData,
    override val required: Boolean
) : LocalizedCommandOption<Role>(languageManager, descriptionI18n), RoleCommandOption