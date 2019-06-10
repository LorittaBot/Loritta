package net.perfectdreams.spicymorenitta.utils

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class GuildConfig constructor(
        @Optional val general: GeneralConfig? = null
)