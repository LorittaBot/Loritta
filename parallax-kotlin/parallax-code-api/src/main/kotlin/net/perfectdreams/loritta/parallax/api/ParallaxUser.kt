package net.perfectdreams.loritta.parallax.api

import kotlinx.serialization.Serializable

@Serializable
class ParallaxUser(
        val id: Long,
        val username: String,
        val discriminator: String,
        val avatar: String
)