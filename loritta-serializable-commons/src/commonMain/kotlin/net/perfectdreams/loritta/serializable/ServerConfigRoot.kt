package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class ServerConfigRoot(
    val id: ULong,
    val localeId: String,
    val starboardConfigId: Long?,
    val miscellaneousConfigId: Long?,
    val inviteBlockerConfigId: Long?
)