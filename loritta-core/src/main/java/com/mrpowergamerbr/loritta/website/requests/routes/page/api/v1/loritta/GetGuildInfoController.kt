package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.utils.MiscUtil
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/loritta/guild/:guildId")
class GetGuildInfoController {
	@GET
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response, guildId: String) {
		res.type(MediaType.json)

		val guild = lorittaShards.getGuildById(guildId)

		if (guild == null) {
			res.send(
					jsonObject()
			)
			return
		}

		res.send(
				jsonObject(
						"id" to guild.id,
						"name" to guild.name,
						"iconUrl" to guild.iconUrl,
						"shardId" to MiscUtil.getShardForGuild(guild, loritta.lorittaShards.shardManager.shards.size),
						"ownerId" to guild.ownerId,
						"region" to guild.region.name,
						"count" to jsonObject(
								"textChannels" to guild.textChannelCache.size(),
								"voiceChannels" to guild.voiceChannelCache.size(),
								"members" to guild.memberCache.size(),
								"onlineMembers" to guild.memberCache.filter { it.onlineStatus == OnlineStatus.ONLINE }.size,
								"idleMembers" to guild.memberCache.filter { it.onlineStatus == OnlineStatus.IDLE }.size,
								"doNotDisturbMembers" to guild.memberCache.filter { it.onlineStatus == OnlineStatus.DO_NOT_DISTURB }.size,
								"offlineMembers" to guild.memberCache.filter { it.onlineStatus == OnlineStatus.OFFLINE }.size,
								"bots" to guild.memberCache.filter { it.user.isBot }.size
						),
						"timeCreated" to guild.timeCreated.toInstant().toEpochMilli(),
						"timeJoined" to guild.selfMember.timeJoined.toInstant().toEpochMilli(),
						"splashUrl" to guild.splashUrl
				)
		)
	}
}