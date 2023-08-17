package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class MiscellaneousConfig(
    val enableBomDiaECia: Boolean,
    val enableQuirky: Boolean,
)