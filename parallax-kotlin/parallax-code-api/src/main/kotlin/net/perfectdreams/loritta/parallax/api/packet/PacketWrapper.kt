package net.perfectdreams.loritta.parallax.api.packet

import kotlinx.serialization.Serializable

@Serializable
data class PacketWrapper(
        val m: ParallaxPacket,
        val uniqueId: Long
)