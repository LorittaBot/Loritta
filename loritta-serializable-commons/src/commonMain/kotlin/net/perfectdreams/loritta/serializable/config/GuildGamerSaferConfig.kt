package net.perfectdreams.loritta.serializable.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.GamerSaferVerificationUserAndRole

@Serializable
data class GuildGamerSaferConfig(
    val verificationRoleId: Long?,
    val verificationRoles: List<GamerSaferVerificationUserAndRole>
)