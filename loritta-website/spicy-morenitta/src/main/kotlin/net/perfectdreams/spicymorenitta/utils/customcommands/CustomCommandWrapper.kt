package net.perfectdreams.spicymorenitta.utils.customcommands

import kotlinx.serialization.Serializable

@Serializable
data class CustomCommandWrapper(
        val data: CustomCommandData
)