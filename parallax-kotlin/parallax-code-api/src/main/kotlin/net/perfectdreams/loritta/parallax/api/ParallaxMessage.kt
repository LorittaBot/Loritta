package net.perfectdreams.loritta.parallax.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.perfectdreams.loritta.api.entities.Guild

@Serializable
class ParallaxMessage(
        val id: Long,
        val author: ParallaxUser,
        val textChannelId: Long,
        val content: String,
        val cleanContent: String,
        val mentionedUsers: List<ParallaxUser>
)