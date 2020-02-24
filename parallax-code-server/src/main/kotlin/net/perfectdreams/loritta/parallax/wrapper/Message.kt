package net.perfectdreams.loritta.parallax.wrapper

import net.perfectdreams.loritta.parallax.ParallaxUtils

class Message(
		val id: Long,
		val author: User,
		val textChannelId: Long,
		val content: String,
		val cleanContent: String,
		val mentionedUsers: List<User>
) {
	lateinit var channel: TextChannel

	override fun toString() = content

	fun reply(message: String) = channel.send("$author, $message")

	fun reply(embed: ParallaxEmbed) = channel.send("$author", embed)

	fun reply(message: Map<*, *>): JavaScriptPromise { // mensagens/embeds em JSON
		val wrapper = ParallaxUtils.toParallaxMessage(message)
		return channel.send(wrapper.content ?: "$author", wrapper.embed)
	}

	fun reply(message: String, embed: ParallaxEmbed?) = channel.send("$author, ", embed)
}