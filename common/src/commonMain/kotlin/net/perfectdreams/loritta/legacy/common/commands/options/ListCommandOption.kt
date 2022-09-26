package net.perfectdreams.loritta.legacy.common.commands.options

import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData

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