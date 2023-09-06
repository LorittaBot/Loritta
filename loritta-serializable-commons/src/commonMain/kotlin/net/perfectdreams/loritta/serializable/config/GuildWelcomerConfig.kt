package net.perfectdreams.loritta.serializable.config

import kotlinx.serialization.Serializable

@Serializable
data class GuildWelcomerConfig(
    val tellOnJoin: Boolean,
    val channelJoinId: Long?,
    val joinMessage: String?,
    val deleteJoinMessagesAfter: Long?,

    val tellOnRemove: Boolean,
    val channelRemoveId: Long?,
    val removeMessage: String?,
    val deleteRemoveMessagesAfter: Long?,

    val tellOnPrivateJoin: Boolean,
    val joinPrivateMessage: String?,

    val tellOnBan: Boolean,
    val bannedMessage: String?
)