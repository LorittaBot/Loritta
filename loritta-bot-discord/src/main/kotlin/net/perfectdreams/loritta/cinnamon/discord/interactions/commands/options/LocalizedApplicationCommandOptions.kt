package net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options

import net.perfectdreams.discordinteraktions.common.autocomplete.FocusedCommandOption
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.ChoiceableCommandOptionBuilder
import net.perfectdreams.discordinteraktions.common.commands.options.CommandChoiceBuilder
import net.perfectdreams.discordinteraktions.common.commands.options.register
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.interactions.autocomplete.AutocompleteContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.autocomplete.CinnamonAutocompleteHandler
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.customoptions.StringListCommandOptionBuilder
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.customoptions.UserListCommandOptionBuilder
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.SlashTextUtils
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.morenitta.LorittaBot

abstract class LocalizedApplicationCommandOptions(val loritta: LorittaBot) : ApplicationCommandOptions() {
    val languageManager = loritta.languageManager

    fun string(
        name: String,
        description: StringI18nData,
        builder: LocalizedStringCommandOptionBuilder.() -> (Unit) = {}
    ) = LocalizedStringCommandOptionBuilder(languageManager, name, description)
        .apply(builder)
        .let { register(it) }

    fun optionalString(
        name: String,
        description: StringI18nData,
        builder: NullableLocalizedStringCommandOptionBuilder.() -> (Unit) = {}
    ) = NullableLocalizedStringCommandOptionBuilder(languageManager, name, description)
        .apply(builder)
        .let { register(it) }

    fun integer(
        name: String,
        description: StringI18nData,
        builder: LocalizedIntegerCommandOptionBuilder.() -> (Unit) = {}
    ) = LocalizedIntegerCommandOptionBuilder(languageManager, name, description)
        .apply(builder)
        .let { register(it) }

    fun optionalInteger(
        name: String,
        description: StringI18nData,
        builder: NullableLocalizedIntegerCommandOptionBuilder.() -> (Unit) = {}
    ) = NullableLocalizedIntegerCommandOptionBuilder(languageManager, name, description)
        .apply(builder)
        .let { register(it) }

    fun number(
        name: String,
        description: StringI18nData,
        builder: LocalizedNumberCommandOptionBuilder.() -> (Unit) = {}
    ) = LocalizedNumberCommandOptionBuilder(languageManager, name, description)
        .apply(builder)
        .let { register(it) }

    fun optionalNumber(
        name: String,
        description: StringI18nData,
        builder: NullableLocalizedNumberCommandOptionBuilder.() -> (Unit) = {}
    ) = NullableLocalizedNumberCommandOptionBuilder(languageManager, name, description)
        .apply(builder)
        .let { register(it) }

    fun boolean(
        name: String,
        description: StringI18nData,
        builder: LocalizedBooleanCommandOptionBuilder.() -> (Unit) = {}
    ) = LocalizedBooleanCommandOptionBuilder(languageManager, name, description)
        .apply(builder)
        .let { register(it) }

    fun optionalBoolean(
        name: String,
        description: StringI18nData,
        builder: NullableLocalizedBooleanCommandOptionBuilder.() -> (Unit) = {}
    ) = NullableLocalizedBooleanCommandOptionBuilder(languageManager, name, description)
        .apply(builder)
        .let { register(it) }

    fun user(
        name: String,
        description: StringI18nData,
        builder: LocalizedUserCommandOptionBuilder.() -> (Unit) = {}
    ) = LocalizedUserCommandOptionBuilder(languageManager, name, description)
        .apply(builder)
        .let { register(it) }

    fun optionalUser(
        name: String,
        description: StringI18nData,
        builder: NullableLocalizedUserCommandOptionBuilder.() -> (Unit) = {}
    ) = NullableLocalizedUserCommandOptionBuilder(languageManager, name, description)
        .apply(builder)
        .let { register(it) }

    fun channel(
        name: String,
        description: StringI18nData,
        builder: LocalizedChannelCommandOptionBuilder.() -> (Unit) = {}
    ) = LocalizedChannelCommandOptionBuilder(languageManager, name, description)
        .apply(builder)
        .let { register(it) }

    fun optionalChannel(
        name: String,
        description: StringI18nData,
        builder: NullableLocalizedChannelCommandOptionBuilder.() -> (Unit) = {}
    ) = NullableLocalizedChannelCommandOptionBuilder(languageManager, name, description)
        .apply(builder)
        .let { register(it) }

    fun role(
        name: String,
        description: StringI18nData,
        builder: LocalizedRoleCommandOptionBuilder.() -> (Unit) = {}
    ) = LocalizedRoleCommandOptionBuilder(languageManager, name, description)
        .apply(builder)
        .let { register(it) }

    fun optionalRole(
        name: String,
        description: StringI18nData,
        builder: NullableLocalizedRoleCommandOptionBuilder.() -> (Unit) = {}
    ) = NullableLocalizedRoleCommandOptionBuilder(languageManager, name, description)
        .apply(builder)
        .let { register(it) }

    fun <T, ChoiceableType> ChoiceableCommandOptionBuilder<T, ChoiceableType>.choice(
        name: StringI18nData,
        value: ChoiceableType,
        block: CommandChoiceBuilder<ChoiceableType>.() -> (Unit) = {}
    ) = choice(languageManager.getI18nContextById("en").get(name).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length), value) {
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
    ) = StringListCommandOptionBuilder(languageManager, name, description, minimum, maximum)
        .apply(builder)
        .let {
            register(it)
        }

    fun userList(
        name: String,
        description: StringI18nData,
        minimum: Int,
        maximum: Int,
        builder: UserListCommandOptionBuilder.() -> (Unit) = {}
    ) = UserListCommandOptionBuilder(languageManager, name, description, minimum, maximum)
        .apply(builder)
        .let {
            register(it)
        }

    fun <T, ChoiceableType> ChoiceableCommandOptionBuilder<T, ChoiceableType>.autocomplete(loritta: LorittaBot, block: suspend (AutocompleteContext, FocusedCommandOption) -> (Map<String, ChoiceableType>))
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