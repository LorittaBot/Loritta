package net.perfectdreams.loritta.cinnamon.discord.commands.options

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

open class ListCommandOptionBuilder<T>(
    // We need to store the command option type due to type erasure
    val type: CommandOptionType,
    val name: String,
    val description: StringI18nData,
    val minimum: Int?,
    val maximum: Int?
)