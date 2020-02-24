package net.perfectdreams.loritta.parallax.wrapper

import com.github.salomonbrys.kotson.set
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.userAgent
import net.perfectdreams.loritta.parallax.ParallaxServer
import net.perfectdreams.loritta.parallax.ParallaxServer.Companion.gson

class Guild(
		val id: Long,
		val name: String,
		val members: List<GuildMember>,
		val channels: List<TextChannel>,
		val roles: List<Role>
) {
	lateinit var context: JSCommandContext

	fun getRoleById(id: String) = roles.firstOrNull { it.id.toString() == id }
	fun getRoleById(id: Long) = roles.firstOrNull { it.id == id }

	fun getTextChannelById(id: String) = channels.firstOrNull { it.id.toString() == id }
	fun getTextChannelById(id: Long) = channels.firstOrNull { it.id == id }

	fun addRoleToMember(member: GuildMember, role: Role): JavaScriptPromise {
		if (role in member.roles)
			return null

		return context.rateLimiter.wrapPromise {
			val response = ParallaxServer.http.put<HttpResponse>("${context.clusterUrl}/api/v1/parallax/guilds/$id/members/${member.id}/roles/${role.id}") {
				this.userAgent(ParallaxServer.USER_AGENT)
				this.header("Authorization", ParallaxServer.authKey)
			}

			null
		}
	}

	fun removeRoleFromMember(member: GuildMember, role: Role): JavaScriptPromise {
		if (role !in member.roles)
			return null

		return context.rateLimiter.wrapPromise {
			val response = ParallaxServer.http.delete<HttpResponse>("${context.clusterUrl}/api/v1/parallax/guilds/$id/members/${member.id}/roles/${role.id}") {
				this.userAgent(ParallaxServer.USER_AGENT)
				this.header("Authorization", ParallaxServer.authKey)
			}

			null
		}
	}

	/* @JvmOverloads
	fun ban(user: User, options: Map<String, Any> = mapOf()) {
		ban(user, ParallaxUser(guild.selfMember.user), options)
	} */

	@JvmOverloads
	fun ban(user: User, punisher: User, options: Map<String, Any> = mapOf()): JavaScriptPromise {
		println("ban(...)")
		return context.rateLimiter.wrapPromise {
			println("${context.clusterUrl}/api/v1/parallax/guilds/$id/bans/${user.id}")
			val response = ParallaxServer.http.put<HttpResponse>("${context.clusterUrl}/api/v1/parallax/guilds/$id/bans/${user.id}") {
				this.userAgent(ParallaxServer.USER_AGENT)
				this.header("Authorization", ParallaxServer.authKey)

				val payload = gson.toJsonTree(options)
				payload["punisher"] = punisher.id
				body = gson.toJson(payload)
			}

			println(response.readText())

			null
		}
	}
}