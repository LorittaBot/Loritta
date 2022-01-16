package net.perfectdreams.loritta.cinnamon.platform.commands.options

class SlashCommandArguments(private val types: Map<CommandOption<*>, Any?>) {
    operator fun <T> get(argument: CommandOption<T>): T {
        if (!types.containsKey(argument) && argument is NullableCommandOption)
            throw RuntimeException("Missing argument ${argument.name}!")

        return types[argument] as T
    }
}