package net.perfectdreams.loritta.parallax.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ParallaxUser(
        val id: Long,
        @SerialName("username")
        val name: String,
        val discriminator: String,
        val avatar: String
)