package net.perfectdreams.loritta.common.commands.options

import net.perfectdreams.loritta.common.locale.LocaleKeyData

open class CommandOptions {
    companion object {
        val NO_OPTIONS = object: CommandOptions() {}
    }

    val arguments = mutableListOf<CommandOption<*>>()

    fun string(name: String, description: LocaleKeyData) = argument<String>(
        CommandOptionType.String,
        name,
        description
    )

    fun optionalString(name: String, description: LocaleKeyData) = argument<String?>(
        CommandOptionType.NullableString,
        name,
        description
    )

    fun integer(name: String, description: LocaleKeyData) = argument<Int>(
        CommandOptionType.Integer,
        name,
        description
    )

    fun optionalInteger(name: String, description: LocaleKeyData) = argument<Int?>(
        CommandOptionType.NullableInteger,
        name,
        description
    )

    fun boolean(name: String, description: LocaleKeyData) = argument<Boolean>(
        CommandOptionType.Bool,
        name,
        description
    )

    fun optionalBoolean(name: String, description: LocaleKeyData) = argument<Boolean?>(
        CommandOptionType.NullableBool,
        name,
        description
    )

    private fun <T> argument(type: CommandOptionType, name: String, description: LocaleKeyData) = CommandOptionBuilder<T>(
        type,
        name,
        description,
        mutableListOf()
    )

    fun <T> CommandOptionBuilder<T>.register(): CommandOption<T> {
        if (arguments.any { it.name == this.name })
            throw IllegalArgumentException("Duplicate argument!")

        val option = CommandOption(
            this.type,
            this.name,
            this.description,
            this.choices
        )

        arguments.add(option)
        return option
    }
}