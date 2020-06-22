package net.perfectdreams.loritta.parallax.api.packet

import kotlinx.serialization.Serializable

@Serializable
class ParallaxPutRolePacket(
        val userId: Long,
        val roleId: Long
) : ParallaxPacket