package net.perfectdreams.loritta.parallax.api

import net.perfectdreams.loritta.utils.Emotes

class ParallaxContext(val guild: ParallaxGuild, val channel: ParallaxMessageChannel, val message: ParallaxMessage, val args: List<String>) {
    val member = guild.members.first { message.author.id == it.user.id }

    fun sendMessage(content: String) = channel.sendMessage(content)

    fun fail(message: String): Nothing = throw ParallaxCommandException(message)

    fun fail(prefix: String, message: String): Nothing = throw ParallaxCommandException(message, prefix)
}