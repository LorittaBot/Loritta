package net.perfectdreams.loritta.common.commands.options

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

class ListCommandOption<T>(
    // We need to store the command option type due to type erasure
    type: CommandOptionType,
    name: String,
    description: StringI18nData,
    val minimum: Int?,
    val maximum: Int?
) : CommandOption<T>(
    type,
    name,
    description,
    listOf()
)