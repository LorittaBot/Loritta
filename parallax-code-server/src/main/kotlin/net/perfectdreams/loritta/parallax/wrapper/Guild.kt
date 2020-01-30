package net.perfectdreams.loritta.parallax.wrapper

import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import io.ktor.http.userAgent
import net.perfectdreams.loritta.parallax.ParallaxServer

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
}