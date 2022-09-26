package net.perfectdreams.loritta.legacy.common.commands.options

import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData

open class ListCommandOptionBuilder<T>(
    // We need to store the command option type due to type erasure
    val type: CommandOptionType,
    val name: String,
    val description: LocaleKeyData,
    val minimum: Int?,
    val maximum: Int?
)