package net.perfectdreams.loritta.cinnamon.platform.commands.options

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

sealed class CommandChoice<T>(
    // We need to store the command option type due to type erasure
    val type: CommandOptionType,
    val value: T
)

class LocalizedCommandChoice<T>(
    // We need to store the command option type due to type erasure
    type: CommandOptionType,
    val name: StringI18nData,
    value: T
) : CommandChoice<T>(type, value)

class RawCommandChoice<T>(
    // We need to store the command option type due to type erasure
    type: CommandOptionType,
    val name: String,
    value: T
) : CommandChoice<T>(type, value)