package net.perfectdreams.loritta.common.commands.options

import net.perfectdreams.loritta.common.locale.LocaleKeyData

class CommandOption<T>(
    val type: CommandOptionType,
    val name: String,
    val description: LocaleKeyData
)