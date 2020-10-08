package net.perfectdreams.loritta.parallax.api.packet

import kotlinx.serialization.Serializable

@Serializable
class ParallaxDeleteRolePacket(
        val userId: Long,
        val roleId: Long
) : ParallaxPacket