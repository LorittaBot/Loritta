package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOption
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptionType

class CommandArguments(private val types: Map<CommandOption<*>, Any?>) {
    operator fun <T> get(argument: CommandOption<T>): T {
        if (!types.containsKey(argument) && argument.type is CommandOptionType.ToNullable)
            throw RuntimeException("Missing argument ${argument.name}!")

        return types[argument] as T
    }
}