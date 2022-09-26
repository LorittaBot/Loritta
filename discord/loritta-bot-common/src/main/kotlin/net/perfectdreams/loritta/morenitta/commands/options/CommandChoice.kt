package net.perfectdreams.loritta.common.commands.options

import net.perfectdreams.loritta.common.locale.LocaleKeyData

class CommandChoice<T>(
    // We need to store the command option type due to type erasure
    val type: CommandOptionType,
    val name: LocaleKeyData,
    val value: T
)