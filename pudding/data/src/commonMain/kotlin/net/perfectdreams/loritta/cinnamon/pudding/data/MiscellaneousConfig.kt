package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable

@Serializable
data class MiscellaneousConfig(
    val enableBomDiaECia: Boolean,
    val enableQuirky: Boolean,
)