package net.perfectdreams.loritta.parallax.api

import kotlinx.serialization.Serializable

@Serializable
class ParallaxGuild(
        val id: Long,
        val name: String,
        val members: List<ParallaxMember>,
        val roles: List<ParallaxRole>
)