package net.perfectdreams.loritta.parallax.wrapper

import com.github.salomonbrys.kotson.jsonObject
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import io.ktor.http.encodeURLQueryComponent
import io.ktor.http.userAgent
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.parallax.ParallaxServer
import net.perfectdreams.loritta.parallax.ParallaxUtils
import java.util.*

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

	fun reply(lorittaReply: JSLorittaReply) = channel.send(
			LorittaReply(lorittaReply.message, lorittaReply.emote, mentionUser = lorittaReply.mentionUser).build(this.author.toString())
	)

	fun reply(vararg lorittaReply: JSLorittaReply) = channel.send(
			lorittaReply.map {
				LorittaReply(it.message, it.emote, mentionUser = it.mentionUser)
			}.map {
				it.build(this.author.toString())
			}.joinToString("\n")
	)

	fun react(reactionCode: String): JavaScriptPromise {
		return channel.guild.context.rateLimiter.wrapPromise {
			val response = ParallaxServer.http.put<HttpResponse>("${channel.guild.context.clusterUrl}/api/v1/parallax/channels/${channel.id}/messages/$id/reactions/${reactionCode.encodeURLQueryComponent()}/@me") {
				this.userAgent(ParallaxServer.USER_AGENT)
				this.header("Authorization", ParallaxServer.authKey)
			}

			null
		}
	}

	fun onReactionAddByUser(reactionCode: String, user: User, function: java.util.function.Function<Void?, Any?>) {
		val trackingId = UUID.randomUUID()
		ParallaxServer.cachedInteractions[trackingId] = function

		runBlocking {
			val response = ParallaxServer.http.put<HttpResponse>("${channel.guild.context.clusterUrl}/api/v1/parallax/channels/${channel.id}/messages/$id/reactions/${reactionCode.encodeURLQueryComponent()}/action") {
				this.userAgent(ParallaxServer.USER_AGENT)
				this.header("Authorization", ParallaxServer.authKey)

				this.body = jsonObject(
						"userId" to user.id,
						"actionType" to "onReactionAddByAuthor",
						"trackingId" to trackingId.toString()
				).toString()
			}
		}
	}
}