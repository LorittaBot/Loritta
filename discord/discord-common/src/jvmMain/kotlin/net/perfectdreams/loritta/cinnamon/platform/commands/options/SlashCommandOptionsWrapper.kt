package net.perfectdreams.loritta.cinnamon.platform.commands.options

import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration

/**
 * Bridge between Cinnamon's [CommandOptions] and Discord InteraKTions' [CommandOptions].
 *
 * Used for argument registering between the two platforms
 */
class SlashCommandOptionsWrapper(
    declarationExecutor: SlashCommandExecutorDeclaration,
    i18nContext: I18nContext
) : ApplicationCommandOptions() {
    companion object {
        private const val MAX_OPTIONS_DESCRIPTION_LENGTH = 100
    }

    init {
        declarationExecutor.options.arguments.forEach {
            when (it) {
                // ===[ SPECIAL CASES ]===
                // Lists are an special case due to Slash Commands not supporting varargs right now
                // As a alternative, we will create from 1 up to 25 options
                is UserListCommandOption -> {
                    val requiredOptions = (it.minimum ?: 0).coerceAtMost(25)
                    val optionalOptions = ((it.maximum ?: 25) - requiredOptions).coerceAtMost(25)

                    var idx = 1

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

                is StringListCommandOption -> {
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
                is ImageReferenceCommandOption -> {
                    // For image references we can accept multiple types
                    // (User Avatar, Link, Emote, etc)
                    // Can't be required because some commands do use optional arguments before this (example: /meme)
                    optionalString(
                        it.name,
                        "User Mention, Image URL or Emote. If not present, I will use an image from the channel!"
                    ).register()

                    // TODO: Fix this later
                    /* optionalBoolean("${it.name}_history", "Image from the most recent message in chat")
                                .register() */
                }

                // ===[ NORMAL ARG TYPES ]===
                else -> {
                    val arg = when (it) {
                        is StringCommandOption -> string(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(it.value as String, i18nContext.get(it.name))
                                    is RawCommandChoice -> option.choice(it.value as String, it.name)
                                }
                            }
                        }

                        is NullableStringCommandOption -> optionalString(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(it.value as String, i18nContext.get(it.name))
                                    is RawCommandChoice -> option.choice(it.value as String, it.name)
                                }
                            }
                        }

                        is IntegerCommandOption -> integer(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(it.value, i18nContext.get(it.name))
                                    is RawCommandChoice -> option.choice(it.value, it.name)
                                }
                            }
                        }

                        is NullableIntegerCommandOption -> optionalInteger(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(it.value, i18nContext.get(it.name))
                                    is RawCommandChoice -> option.choice(it.value, it.name)
                                }
                            }
                        }

                        is NumberCommandOption -> number(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(it.value, i18nContext.get(it.name))
                                    is RawCommandChoice -> option.choice(it.value, it.name)
                                }
                            }
                        }

                        is NullableNumberCommandOption -> optionalNumber(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(it.value, i18nContext.get(it.name))
                                    is RawCommandChoice -> option.choice(it.value, it.name)
                                }
                            }
                        }

                        is BooleanCommandOption -> boolean(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is NullableBooleanCommandOption -> optionalBoolean(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is UserCommandOption -> user(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is NullableUserCommandOption -> optionalUser(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is ChannelCommandOption -> channel(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is NullableChannelCommandOption -> optionalChannel(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is RoleCommandOption -> role(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is NullableRoleCommandOption -> optionalRole(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        else -> throw UnsupportedOperationException("Unsupported option type ${it::class}")
                    }

                    arg.register()
                }
            }
        }
    }
}