package net.perfectdreams.loritta.parallax.api

class ParallaxContext(val guild: ParallaxGuild, val channel: ParallaxMessageChannel, val args: List<String>) {
    fun sendMessage(content: String) = channel.sendMessage(content)
}