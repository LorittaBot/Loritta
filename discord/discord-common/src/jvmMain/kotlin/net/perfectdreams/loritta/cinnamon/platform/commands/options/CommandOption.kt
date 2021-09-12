package net.perfectdreams.loritta.cinnamon.platform.commands.options

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.i18nhelper.core.keys.StringI18nKey

open class CommandOption<T>(
    // We need to store the command option type due to type erasure
    val type: CommandOptionType,
    val name: String,
    val description: StringI18nData,
    val choices: List<CommandChoice<T>>
)