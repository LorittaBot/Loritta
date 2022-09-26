package net.perfectdreams.loritta.common.commands.options

import net.perfectdreams.loritta.common.locale.LocaleKeyData

open class CommandOptionBuilder<T>(
    // We need to store the command option type due to type erasure
    val type: CommandOptionType,
    val name: String,
    val description: LocaleKeyData,
    val choices: MutableList<CommandChoice<T>>
) {
    fun choice(value: T, name: LocaleKeyData): CommandOptionBuilder<T> {
        choices.add(
            CommandChoice(
                type,
                name,
                value
            )
        )
        return this
    }
}