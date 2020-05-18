package net.perfectdreams.loritta.parallax.wrapper

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.userAgent
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.parallax.ParallaxServer
import net.perfectdreams.loritta.parallax.ParallaxServer.Companion.gson
import net.perfectdreams.loritta.parallax.ParallaxUtils

class TextChannel(
		val id: Long,
		val name: String
) {
	lateinit var guild: Guild

	fun send(message: String) = send(message, null)

	fun send(embed: ParallaxEmbed) = send(" ", embed)

	fun send(message: Map<*, *>) { // mensagens/embeds em JSON
		val wrapper = ParallaxUtils.toParallaxMessage(message)
		return send(wrapper.content ?: " ", wrapper.embed)
	}

	fun send(message: String, embed: ParallaxEmbed?) {
		guild.context.rateLimiter.addAndCheck()

		val body = jsonObject(
				"content" to message
		)

		if (embed != null)
			body["embed"] = gson.toJsonTree(embed)

		val payload = gson.toJson(body)

		runBlocking {
			val response = ParallaxServer.http.post<HttpResponse>("${guild.context.clusterUrl}/api/v1/parallax/channels/${id}/messages") {
				this.userAgent(ParallaxServer.USER_AGENT)
				this.header("Authorization", ParallaxServer.authKey)

				this.body = payload
			}

			val message = ParallaxServer.gson.fromJson<Message>(response.readText())
			message.channel = guild.channels.first { it.id == message.textChannelId }
			message
		}
	}

	override fun toString() = "<#$id>"
}