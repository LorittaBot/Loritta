package net.perfectdreams.loritta.cinnamon.platform.commands.options

import dev.kord.common.Locale
import net.perfectdreams.discordinteraktions.common.autocomplete.AutocompleteHandler
import net.perfectdreams.discordinteraktions.common.autocomplete.FocusedCommandOption
import net.perfectdreams.discordinteraktions.common.commands.options.*
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.AutocompleteContext
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.CinnamonAutocompleteHandler
import net.perfectdreams.loritta.cinnamon.platform.commands.customoptions.ImageReferenceCommandOptionBuilder
import net.perfectdreams.loritta.cinnamon.platform.commands.customoptions.ImageReferenceOrAttachmentOptionBuilder
import net.perfectdreams.loritta.cinnamon.platform.commands.customoptions.StringListCommandOptionBuilder
import net.perfectdreams.loritta.cinnamon.platform.commands.customoptions.UserListCommandOptionBuilder
import net.perfectdreams.loritta.cinnamon.platform.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.platform.utils.I18nContextUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.SlashTextUtils

abstract class LocalizedApplicationCommandOptions(val loritta: LorittaCinnamon) : ApplicationCommandOptions() {
    val languageManager = loritta.languageManager

    fun string(
        name: String,
        description: StringI18nData,
        builder: StringCommandOptionBuilder.() -> (Unit) = {}
    ) = string(name, languageManager.defaultI18nContext.get(description).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)) {
        apply(builder)

        descriptionLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, description)
    }

    fun optionalString(
        name: String,
        description: StringI18nData,
        builder: NullableStringCommandOptionBuilder.() -> (Unit) = {}
    ) = optionalString(name, languageManager.defaultI18nContext.get(description).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)) {
        apply(builder)

        descriptionLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, description)
    }

    fun integer(
        name: String,
        description: StringI18nData,
        builder: IntegerCommandOptionBuilder.() -> (Unit) = {}
    ) = integer(name, languageManager.defaultI18nContext.get(description).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)) {
        apply(builder)

        descriptionLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, description)
    }

    fun optionalInteger(
        name: String,
        description: StringI18nData,
        builder: NullableIntegerCommandOptionBuilder.() -> (Unit) = {}
    ) = optionalInteger(name, languageManager.defaultI18nContext.get(description).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)) {
        apply(builder)

        descriptionLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, description)
    }

    fun number(
        name: String,
        description: StringI18nData,
        builder: NumberCommandOptionBuilder.() -> (Unit) = {}
    ) = number(name, languageManager.defaultI18nContext.get(description).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)) {
        apply(builder)

        descriptionLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, description)
    }

    fun optionalNumber(
        name: String,
        description: StringI18nData,
        builder: NullableNumberCommandOptionBuilder.() -> (Unit) = {}
    ) = optionalNumber(name, languageManager.defaultI18nContext.get(description).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)) {
        apply(builder)

        descriptionLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, description)
    }

    fun user(
        name: String,
        description: StringI18nData,
        builder: UserCommandOptionBuilder.() -> (Unit) = {}
    ) = user(name, languageManager.defaultI18nContext.get(description).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)) {
        apply(builder)

        descriptionLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, description)
    }

    fun optionalUser(
        name: String,
        description: StringI18nData,
        builder: NullableUserCommandOptionBuilder.() -> (Unit) = {}
    ) = optionalUser(name, languageManager.defaultI18nContext.get(description).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)) {
        apply(builder)

        descriptionLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, description)
    }

    fun channel(
        name: String,
        description: StringI18nData,
        builder: ChannelCommandOptionBuilder.() -> (Unit) = {}
    ) = channel(name, languageManager.defaultI18nContext.get(description).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)) {
        apply(builder)

        descriptionLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, description)
    }

    fun optionalChannel(
        name: String,
        description: StringI18nData,
        builder: NullableChannelCommandOptionBuilder.() -> (Unit) = {}
    ) = optionalChannel(name, languageManager.defaultI18nContext.get(description).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)) {
        apply(builder)

        descriptionLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, description)
    }

    fun role(
        name: String,
        description: StringI18nData,
        builder: RoleCommandOptionBuilder.() -> (Unit) = {}
    ) = role(name, languageManager.defaultI18nContext.get(description).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)) {
        apply(builder)

        descriptionLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, description)
    }

    fun optionalRole(
        name: String,
        description: StringI18nData,
        builder: NullableRoleCommandOptionBuilder.() -> (Unit) = {}
    ) = optionalRole(name, languageManager.defaultI18nContext.get(description).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length)) {
        apply(builder)

        descriptionLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, description)
    }

    fun <T, ChoiceableType> ChoiceableCommandOptionBuilder<T, ChoiceableType>.choice(
        name: StringI18nData,
        value: ChoiceableType,
        block: CommandChoiceBuilder<ChoiceableType>.() -> (Unit) = {}
    ) = choice(languageManager.defaultI18nContext.get(name).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length), value) {
        nameLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, name)
        block.invoke(this)
    }

    // ===[ CUSTOM OPTIONS ]===
    fun stringList(
        name: String,
        description: StringI18nData,
        minimum: Int,
        maximum: Int,
        builder: StringListCommandOptionBuilder.() -> (Unit) = {}
    ) = StringListCommandOptionBuilder(name, languageManager.defaultI18nContext.get(description).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length), minimum, maximum)
        .apply(builder)
        .let {
            it.descriptionLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, description)
            register(it)
        }

    fun userList(
        name: String,
        description: StringI18nData,
        minimum: Int,
        maximum: Int,
        builder: UserListCommandOptionBuilder.() -> (Unit) = {}
    ) = UserListCommandOptionBuilder(name, languageManager.defaultI18nContext.get(description).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length), minimum, maximum)
        .apply(builder)
        .let {
            it.descriptionLocalizations = SlashTextUtils.createShortenedLocalizedStringMapExcludingDefaultLocale(languageManager, description)
            register(it)
        }

    fun imageReference(
        name: String,
        builder: ImageReferenceCommandOptionBuilder.() -> (Unit) = {}
    ) = ImageReferenceCommandOptionBuilder(name, "Image, URL or Emoji", true)
        .apply(builder)
        .let {
            it.descriptionLocalizations = SlashTextUtils.shortenAll(it.descriptionLocalizations)
            register(it)
        }

    fun imageReferenceOrAttachment(
        name: String,
        builder: ImageReferenceOrAttachmentOptionBuilder.() -> (Unit) = {}
    ) = ImageReferenceOrAttachmentOptionBuilder(name, true)
        .apply(builder)
        .let {
            it.descriptionLocalizations = SlashTextUtils.shortenAll(it.descriptionLocalizations)
            register(it)
        }

    fun optionalImageReferenceOrAttachment(
        name: String,
        builder: ImageReferenceOrAttachmentOptionBuilder.() -> (Unit) = {}
    ) = ImageReferenceOrAttachmentOptionBuilder(name, false)
        .apply(builder)
        .let {
            it.descriptionLocalizations = SlashTextUtils.shortenAll(it.descriptionLocalizations)
            register(it)
        }

    fun <T, ChoiceableType> ChoiceableCommandOptionBuilder<T, ChoiceableType>.autocomplete(loritta: LorittaCinnamon, block: suspend (AutocompleteContext, FocusedCommandOption) -> (Map<String, ChoiceableType>))
            = autocomplete(
        object: CinnamonAutocompleteHandler<ChoiceableType>(loritta) {
            override suspend fun handle(
                context: AutocompleteContext,
                focusedOption: FocusedCommandOption
            ): Map<String, ChoiceableType> {
                return block.invoke(context, focusedOption)
            }
        }
    )

    fun <T, ChoiceableType> ChoiceableCommandOptionBuilder<T, ChoiceableType>.cinnamonAutocomplete(block: suspend (AutocompleteContext, FocusedCommandOption) -> (Map<String, ChoiceableType>))
            = autocomplete(
        object: CinnamonAutocompleteHandler<ChoiceableType>(loritta) {
            override suspend fun handle(
                context: AutocompleteContext,
                focusedOption: FocusedCommandOption
            ): Map<String, ChoiceableType> {
                return block.invoke(context, focusedOption)
            }
        }
    )
}