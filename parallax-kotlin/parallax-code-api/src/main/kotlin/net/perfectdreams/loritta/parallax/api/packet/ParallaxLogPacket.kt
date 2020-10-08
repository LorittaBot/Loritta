package net.perfectdreams.loritta.parallax.api.packet

import kotlinx.serialization.Serializable

@Serializable
class ParallaxLogPacket(
        val message: String
) : ParallaxPacket