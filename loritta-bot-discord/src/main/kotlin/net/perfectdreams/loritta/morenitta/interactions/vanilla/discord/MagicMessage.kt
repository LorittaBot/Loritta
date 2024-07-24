package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import kotlinx.serialization.Serializable

@Serializable
data class MagicMessage(
    val userId: Long,
    val content: String
)