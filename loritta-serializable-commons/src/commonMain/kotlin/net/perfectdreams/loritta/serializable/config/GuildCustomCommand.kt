package net.perfectdreams.loritta.serializable.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.CustomCommandCodeType

@Serializable
data class GuildCustomCommand(
    val id: Long,
    val label: String,
    val codeType: CustomCommandCodeType,
    val code: String
)