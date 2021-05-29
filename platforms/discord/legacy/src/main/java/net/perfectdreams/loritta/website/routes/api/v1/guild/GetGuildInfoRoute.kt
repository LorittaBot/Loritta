package net.perfectdreams.loritta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import net.dv8tion.jda.api.OnlineStatus
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class GetGuildInfoRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/guilds/{guildId}") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val guildId = call.parameters["guildId"] ?: return

		val guild = lorittaShards.getGuildById(guildId)

		if (guild == null) {
			call.respondJson(
					jsonObject()
			)
			return
		}

		call.respondJson(
				jsonObject(
						"id" to guild.id,
						"name" to guild.name,
						"iconUrl" to guild.iconUrl,
						"shardId" to guild.jda.shardInfo.shardId,
						"ownerId" to guild.ownerId,
						"region" to guild.region.name,
						"count" to jsonObject(
								"textChannels" to guild.textChannelCache.size(),
								"voiceChannels" to guild.voiceChannelCache.size(),
								"members" to guild.memberCount,
								"onlineMembers" to guild.memberCache.filter { it.onlineStatus == OnlineStatus.ONLINE }.size,
								"idleMembers" to guild.memberCache.filter { it.onlineStatus == OnlineStatus.IDLE }.size,
								"doNotDisturbMembers" to guild.memberCache.filter { it.onlineStatus == OnlineStatus.DO_NOT_DISTURB }.size,
								"offlineMembers" to guild.memberCache.filter { it.onlineStatus == OnlineStatus.OFFLINE }.size,
								"bots" to guild.memberCache.filter { it.user.isBot }.size
						),
						"timeCreated" to guild.timeCreated.toInstant().toEpochMilli(),
						"timeJoined" to guild.selfMember.timeJoined.toInstant().toEpochMilli(),
						"splashUrl" to guild.splashUrl,
						"boostCount" to guild.boostCount
				)
		)
	}
}