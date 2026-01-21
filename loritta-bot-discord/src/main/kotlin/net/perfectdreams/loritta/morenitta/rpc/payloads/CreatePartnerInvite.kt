package net.perfectdreams.loritta.morenitta.rpc.payloads

import kotlinx.serialization.Serializable

@Serializable
data class CreatePartnerInviteRequest(
    val userId: Long,
    val partnerGuildId: Long,
    val partnerInviteChannelId: Long
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
