package net.perfectdreams.spicymorenitta.utils.customcommands

import kotlinx.serialization.Serializable

@Serializable
class AutoroleCustomCommand(
        val roleId: Long
) : CustomCommandData