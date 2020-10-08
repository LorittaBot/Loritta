package net.perfectdreams.loritta.parallax.api.packet

import kotlinx.serialization.Serializable

@Serializable
class ParallaxThrowablePacket(
        val message: String
) : ParallaxPacket