package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.discordinteraktions.declarations.commands.slash.options.CommandOptions
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptionType
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ListCommandOption
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedCommandChoice
import net.perfectdreams.loritta.cinnamon.platform.commands.options.RawCommandChoice

/**
 * Bridge between Cinnamon's [CommandOptions] and Discord InteraKTions' [CommandOptions].
 *
 * Used for argument registering between the two platforms
 */
class SlashCommandOptionsWrapper(
    declarationExecutor: CommandExecutorDeclaration,
    i18nContext: I18nContext
) : CommandOptions() {
    companion object {
        private const val MAX_OPTIONS_DESCRIPTION_LENGTH = 100
    }

    init {
        declarationExecutor.options.arguments.forEach {
            when {
                // ===[ SPECIAL CASES ]===
                it is ListCommandOption<*> -> {
                    // String List is a special case due to Slash Commands not supporting varargs right now
                    // As a alternative, we will create from 1 up to 25 options
                    val requiredOptions = (it.minimum ?: 0).coerceAtMost(25)
                    val optionalOptions = ((it.maximum ?: 25) - requiredOptions).coerceAtMost(25)

                    var idx = 1

                    when (it.type) {
                        CommandOptionType.UserList -> {
                            repeat(requiredOptions) { _ ->
                                user("${it.name}$idx", i18nContext.get(it.description))
                                    .register()
                                idx++
                            }

                            repeat(optionalOptions) { _ ->
                                optionalUser("${it.name}$idx", i18nContext.get(it.description))
                                    .register()
                                idx++
                            }
                        }

                        else -> {
                            repeat(requiredOptions) { _ ->
                                string("${it.name}$idx", i18nContext.get(it.description))
                                    .register()
                                idx++
                            }

                            repeat(optionalOptions) { _ ->
                                optionalString("${it.name}$idx", i18nContext.get(it.description))
                                    .register()
                                idx++
                            }
                        }
                    }
                }

                it.type == CommandOptionType.ImageReference -> {
                    // For image references we can accept multiple types
                    // (User Avatar, Link, Emote, etc)
                    // Can't be required because some commands do use optional arguments before this (example: /meme)
                    optionalString(
                        it.name,
                        "User Mention, Image URL or Emote. If not present, I will use an image from the channel!"
                    )
                        .register()

                    // TODO: Fix this later
                    /* optionalBoolean("${it.name}_history", "Image from the most recent message in chat")
                        .register() */
                }

                // ===[ NORMAL ARG TYPES ]===
                else -> {
                    val arg = when (it.type) {
                        is CommandOptionType.String -> string(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(it.value as String, i18nContext.get(it.name))
                                    is RawCommandChoice -> option.choice(it.value as String, it.name)
                                }
                            }

                            if (it.autoCompleteExecutorDeclaration != null) {
                                option.autocomplete(
                                    object : net.perfectdreams.discordinteraktions.common.autocomplete.StringAutocompleteExecutorDeclaration(
                                        it.autoCompleteExecutorDeclaration::class
                                    ) {}
                                )
                            }
                        }

                        is CommandOptionType.NullableString -> optionalString(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(it.value as String, i18nContext.get(it.name))
                                    is RawCommandChoice -> option.choice(it.value as String, it.name)
                                }
                            }

                            if (it.autoCompleteExecutorDeclaration != null) {
                                /* option.autocomplete(
                                    object : net.perfectdreams.discordinteraktions.common.autocomplete.StringAutocompleteExecutorDeclaration(
                                        it.autoCompleteExecutorDeclaration::class
                                    ) {}
                                ) */
                            }
                        }

                        is CommandOptionType.Integer -> integer(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(it.value as Long, i18nContext.get(it.name))
                                    is RawCommandChoice -> option.choice(it.value as Long, it.name)
                                }
                            }

                            if (it.autoCompleteExecutorDeclaration != null) {
                                option.autocomplete(
                                    object : net.perfectdreams.discordinteraktions.common.autocomplete.IntegerAutocompleteExecutorDeclaration(
                                        it.autoCompleteExecutorDeclaration::class
                                    ) {}
                                )
                            }
                        }

                        is CommandOptionType.NullableInteger -> optionalInteger(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(it.value as Long, i18nContext.get(it.name))
                                    is RawCommandChoice -> option.choice(it.value as Long, it.name)
                                }
                            }

                            if (it.autoCompleteExecutorDeclaration != null) {
                                /* option.autocomplete(
                                    object : net.perfectdreams.discordinteraktions.common.autocomplete.IntegerAutocompleteExecutorDeclaration(
                                        it.autoCompleteExecutorDeclaration::class
                                    ) {}
                                ) */
                            }
                        }

                        is CommandOptionType.Number -> number(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(it.value as Double, i18nContext.get(it.name))
                                    is RawCommandChoice -> option.choice(it.value as Double, it.name)
                                }
                            }

                            if (it.autoCompleteExecutorDeclaration != null) {
                                option.autocomplete(
                                    object : net.perfectdreams.discordinteraktions.common.autocomplete.NumberAutocompleteExecutorDeclaration(
                                        it.autoCompleteExecutorDeclaration::class
                                    ) {}
                                )
                            }
                        }

                        is CommandOptionType.NullableNumber -> optionalNumber(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(it.value as Double?, i18nContext.get(it.name))
                                    is RawCommandChoice -> option.choice(it.value as Double?, it.name)
                                }
                            }

                            if (it.autoCompleteExecutorDeclaration != null) {
                                /* option.autocomplete(
                                    object : net.perfectdreams.discordinteraktions.common.autocomplete.NumberAutocompleteExecutorDeclaration(
                                        it.autoCompleteExecutorDeclaration::class
                                    ) {}
                                ) */
                            }
                        }

                        is CommandOptionType.Bool -> boolean(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is CommandOptionType.NullableBool -> optionalBoolean(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is CommandOptionType.User -> user(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is CommandOptionType.NullableUser -> optionalUser(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is CommandOptionType.Channel -> channel(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is CommandOptionType.NullableChannel -> optionalChannel(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is CommandOptionType.Role -> role(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is CommandOptionType.NullableRole -> optionalRole(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        else -> throw UnsupportedOperationException("Unsupported option type ${it.type}")
                    }

                    arg.register()
                }
            }
        }
    }
}