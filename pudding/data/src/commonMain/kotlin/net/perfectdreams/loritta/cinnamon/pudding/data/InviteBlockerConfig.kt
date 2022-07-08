package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable

@Serializable
data class InviteBlockerConfig(
    val enabled: Boolean,
    val whitelistedChannels: List<Long>,
    val whitelistServerInvites: Boolean,
    val deleteMessage: Boolean,
    val tellUser: Boolean,
    val warnMessage: String?
)