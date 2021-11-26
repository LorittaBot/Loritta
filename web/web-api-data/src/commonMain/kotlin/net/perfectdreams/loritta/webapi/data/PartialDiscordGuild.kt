package net.perfectdreams.loritta.webapi.data

import kotlinx.serialization.Serializable

@Serializable
class PartialDiscordGuild(
    val id: ULong,
    val name: String,
    val icon: String?,
    val owner: Boolean,
    val permissions: String,
    val features: List<String>
)