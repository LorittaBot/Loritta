package net.perfectdreams.loritta.common.commands.options

import net.perfectdreams.loritta.common.locale.LocaleKeyData

class ListCommandOption<T>(
    // We need to store the command option type due to type erasure
    type: CommandOptionType,
    name: String,
    description: LocaleKeyData,
    val minimum: Int?,
    val maximum: Int?
) : CommandOption<T>(
    type,
    name,
    description,
    listOf()
)