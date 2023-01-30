package net.perfectdreams.loritta.morenitta.utils.gamersafer

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class GuildInviteEvent(
    val event: String,
    val payload: Payload,
    val ageGroup: JsonObject,
    val player: JsonObject
) {
    @Serializable
    data class Payload(
        val code: String,
        val guildMemberId: String,
        val internalId: String,
        val discordMessage: String
    )
}