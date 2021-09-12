package net.perfectdreams.loritta.cinnamon.platform.commands.options

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

class CommandChoice<T>(
    // We need to store the command option type due to type erasure
    val type: CommandOptionType,
    val name: StringI18nData,
    val value: T
)