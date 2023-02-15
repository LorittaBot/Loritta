package net.perfectdreams.loritta.morenitta.utils.gamersafer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
sealed class GamerSaferEvent

@Serializable
@SerialName("playerVerification")
data class PlayerVerificationEvent(
    val event: String,
    val payload: Payload
) : GamerSaferEvent() {
    @Serializable
    data class Payload(
        val guildMemberId: String,
        val verified: Boolean,
        val discordMessage: String
    )
}

@Serializable
@SerialName("guildInvite")
data class GuildInviteEvent(
    val event: String,
    val payload: Payload,
    val ageGroup: JsonObject,
    val player: JsonObject
) : GamerSaferEvent() {
    @Serializable
    data class Payload(
        val code: String,
        val guildMemberId: String,
        val internalId: String,
        val discordMessage: String
    )
}