package net.perfectdreams.loritta.cinnamon.platform.commands.options

import net.perfectdreams.discordinteraktions.common.commands.options.*
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
        const val MAX_OPTIONS_DESCRIPTION_LENGTH = 100
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
                        it.name + "_data",
                        "User, URL or Emote"
                    ).register()

                    optionalAttachment(
                        it.name + "_file",
                        "Image Attachment"
                    ).register()
                }

                // ===[ NORMAL ARG TYPES ]===
                else -> {
                    val arg = when (it) {
                        is StringCommandOption -> if (it.required) string(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(
                                        it.value as String,
                                        i18nContext.get(it.name)
                                    )
                                    is RawCommandChoice -> option.choice(it.value as String, it.name)
                                }
                            }

                            if (it.autoCompleteExecutorDeclaration != null) {
                                option.autocomplete(
                                    object :
                                        net.perfectdreams.discordinteraktions.common.autocomplete.StringAutocompleteExecutorDeclaration(
                                            it.autoCompleteExecutorDeclaration::class
                                        ) {}
                                )
                            }
                        } else optionalString(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(
                                        it.value as String,
                                        i18nContext.get(it.name)
                                    )
                                    is RawCommandChoice -> option.choice(it.value as String, it.name)
                                }
                            }

                            if (it.autoCompleteExecutorDeclaration != null) {
                                option.autocomplete(
                                    object :
                                        net.perfectdreams.discordinteraktions.common.autocomplete.StringAutocompleteExecutorDeclaration(
                                            it.autoCompleteExecutorDeclaration::class
                                        ) {}
                                )
                            }
                        }

                        is IntegerCommandOption -> if (it.required) integer(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(it.value, i18nContext.get(it.name))
                                    is RawCommandChoice -> option.choice(it.value, it.name)
                                }
                            }

                            if (it.autoCompleteExecutorDeclaration != null) {
                                option.autocomplete(
                                    object :
                                        net.perfectdreams.discordinteraktions.common.autocomplete.IntegerAutocompleteExecutorDeclaration(
                                            it.autoCompleteExecutorDeclaration::class
                                        ) {}
                                )
                            }
                        } else optionalInteger(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(it.value, i18nContext.get(it.name))
                                    is RawCommandChoice -> option.choice(it.value, it.name)
                                }
                            }

                            if (it.autoCompleteExecutorDeclaration != null) {
                                option.autocomplete(
                                    object :
                                        net.perfectdreams.discordinteraktions.common.autocomplete.IntegerAutocompleteExecutorDeclaration(
                                            it.autoCompleteExecutorDeclaration::class
                                        ) {}
                                )
                            }
                        }

                        is NumberCommandOption -> if (it.required) number(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(it.value, i18nContext.get(it.name))
                                    is RawCommandChoice -> option.choice(it.value, it.name)
                                }
                            }

                            if (it.autoCompleteExecutorDeclaration != null) {
                                option.autocomplete(
                                    object :
                                        net.perfectdreams.discordinteraktions.common.autocomplete.NumberAutocompleteExecutorDeclaration(
                                            it.autoCompleteExecutorDeclaration::class
                                        ) {}
                                )
                            }
                        } else optionalNumber(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ).also { option ->
                            it.choices.take(25).forEach {
                                when (it) {
                                    is LocalizedCommandChoice -> option.choice(it.value, i18nContext.get(it.name))
                                    is RawCommandChoice -> option.choice(it.value, it.name)
                                }
                            }

                            if (it.autoCompleteExecutorDeclaration != null) {
                                option.autocomplete(
                                    object :
                                        net.perfectdreams.discordinteraktions.common.autocomplete.NumberAutocompleteExecutorDeclaration(
                                            it.autoCompleteExecutorDeclaration::class
                                        ) {}
                                )
                            }
                        }

                        is BooleanCommandOption -> if (it.required) boolean(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ) else optionalBoolean(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is UserCommandOption -> if (it.required) user(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ) else optionalUser(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is ChannelCommandOption -> if (it.required) channel(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ) else optionalChannel(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        )

                        is RoleCommandOption -> if (it.required) role(
                            it.name,
                            i18nContext.get(it.description).shortenWithEllipsis(MAX_OPTIONS_DESCRIPTION_LENGTH)
                        ) else optionalRole(
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