package net.perfectdreams.loritta.morenitta.rpc.payloads

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.partnerapplications.PartnerPermissionLevel

@Serializable
data class CreatePartnerInviteRequest(
    val userId: Long,
    val requestedForGuildId: Long,
    val partnerGuildId: Long,
    val partnerInviteChannelId: Long,
    val generatorPermissionLevel: PartnerPermissionLevel
)

@Serializable
sealed class CreatePartnerInviteResponse {
    @Serializable
    data class Success(val inviteCode: String) : CreatePartnerInviteResponse()

    @Serializable
    data object GuildNotFound : CreatePartnerInviteResponse()

    @Serializable
    data object ChannelNotFound : CreatePartnerInviteResponse()

    @Serializable
    data object MissingPermissions : CreatePartnerInviteResponse()

    @Serializable
    data object InviteCreationFailed : CreatePartnerInviteResponse()
}
