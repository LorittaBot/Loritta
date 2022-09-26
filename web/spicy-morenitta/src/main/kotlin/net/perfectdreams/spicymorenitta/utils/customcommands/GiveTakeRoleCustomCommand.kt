package net.perfectdreams.spicymorenitta.utils.customcommands

import kotlinx.serialization.Serializable

@Serializable
class GiveTakeRoleCustomCommand(
        val roleId: Long,
        val roleGivenMessage: String,
        val roleTakenMessage: String
) : CustomCommandData