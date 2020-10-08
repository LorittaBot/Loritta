package net.perfectdreams.loritta.parallax.api.packet

import kotlinx.serialization.Serializable

@Serializable
class ParallaxSendMessagePacket(
        val content: String
) : ParallaxPacket