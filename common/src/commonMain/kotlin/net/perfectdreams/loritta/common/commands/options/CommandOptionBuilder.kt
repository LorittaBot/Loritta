package net.perfectdreams.loritta.common.commands.options

import net.perfectdreams.loritta.common.locale.LocaleKeyData

class CommandOptionBuilder<T>(
    // We need to store the command option type due to type erasure
    val type: CommandOptionType,
    val name: String,
    val description: LocaleKeyData
) {
    fun optional(): CommandOptionBuilder<T?> {
        if (type !is CommandOptionType.ToNullable)
            throw IllegalArgumentException("$type cannot be optional!")

        return CommandOptionBuilder(
            type.toNullable(),
            name,
            description
        )
    }
}