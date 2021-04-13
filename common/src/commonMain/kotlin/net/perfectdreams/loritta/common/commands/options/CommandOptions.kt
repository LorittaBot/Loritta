package net.perfectdreams.loritta.common.commands.options

import net.perfectdreams.loritta.common.locale.LocaleKeyData

open class CommandOptions {
    companion object {
        val NO_OPTIONS = object: CommandOptions() {}
    }

    val arguments = mutableListOf<CommandOption<*>>()

    fun integer(name: String, description: LocaleKeyData) = CommandOptionBuilder<Int>(
        CommandOptionType.Integer,
        name,
        description
    )

    fun <T> CommandOptionBuilder<T>.register(): CommandOption<T> {
        if (arguments.any { it.name == this.name })
            throw IllegalArgumentException("Duplicate argument!")

        val option = CommandOption<T>(
            this.type,
            this.name,
            this.description
        )
        arguments.add(option)
        return option
    }
}