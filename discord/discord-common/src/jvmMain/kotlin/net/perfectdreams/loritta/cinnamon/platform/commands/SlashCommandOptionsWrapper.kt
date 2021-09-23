package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.discordinteraktions.declarations.commands.slash.options.CommandOptions
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptionType
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ListCommandOption

/**
 * Bridge between Cinnamon's [CommandOptions] and Discord InteraKTions' [CommandOptions].
 *
 * Used for argument registering between the two platforms
 */
class SlashCommandOptionsWrapper(
    declarationExecutor: CommandExecutorDeclaration,
    i18nContext: I18nContext
) : CommandOptions() {
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

                it.type == CommandOptionType.ImageReference -> {
                    // For image references we can accept multiple types
                    // (User Avatar, Link, Emote, etc)
                    // Can't be required because some commands do use optional arguments before this (example: /meme)
                    string(it.name, "User Mention, Image URL or Emote")
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
                            i18nContext.get(it.description).shortenWithEllipsis()
                        ).also { option ->
                            it.choices.take(25).forEach {
                                option.choice(it.value as String, i18nContext.get(it.name))
                            }
                        }

                        is CommandOptionType.NullableString -> optionalString(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis()
                        ).also { option ->
                            it.choices.take(25).forEach {
                                option.choice(it.value as String, i18nContext.get(it.name))
                            }
                        }

                        is CommandOptionType.Integer -> integer(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis()
                        ).also { option ->
                            it.choices.take(25).forEach {
                                option.choice(it.value as Int, i18nContext.get(it.name))
                            }
                        }

                        is CommandOptionType.NullableInteger -> optionalInteger(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis()
                        ).also { option ->
                            it.choices.take(25).forEach {
                                option.choice(it.value as Int, i18nContext.get(it.name))
                            }
                        }

                        is CommandOptionType.Number -> number(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis()
                        ).also { option ->
                            it.choices.take(25).forEach {
                                option.choice(it.value as Double, i18nContext.get(it.name))
                            }
                        }

                        is CommandOptionType.NullableNumber -> optionalNumber(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis()
                        ).also { option ->
                            it.choices.take(25).forEach {
                                option.choice(it.value as Double?, i18nContext.get(it.name))
                            }
                        }

                        is CommandOptionType.Bool -> boolean(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis()
                        )

                        is CommandOptionType.NullableBool -> optionalBoolean(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis()
                        )

                        is CommandOptionType.User -> user(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis()
                        )

                        is CommandOptionType.NullableUser -> optionalUser(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis()
                        )

                        is CommandOptionType.Channel -> optionalChannel(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis()
                        )

                        is CommandOptionType.NullableChannel -> optionalChannel(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis()
                        )

                        else -> throw UnsupportedOperationException("Unsupported option type ${it.type}")
                    }

                    arg.register()
                }
            }
        }
    }
}