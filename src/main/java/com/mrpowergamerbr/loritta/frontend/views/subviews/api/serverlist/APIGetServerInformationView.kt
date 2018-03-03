package com.mrpowergamerbr.loritta.frontend.views.subviews.api.serverlist

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.frontend.views.LoriWebCodes
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.NoVarsView
import com.mrpowergamerbr.loritta.userdata.ServerListConfig
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.OnlineStatus
import org.jooby.Request
import org.jooby.Response
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import java.io.File
import java.util.*

class APIGetServerInformationView : NoVarsView() {
	override fun handleRender(req: Request, res: Response): Boolean {
		return req.path().matches(Regex("^/api/v1/server-list/information"))
	}

	override fun render(req: Request, res: Response): String {
		var userIdentification: TemmieDiscordAuth.UserIdentification? = null
		if (req.session().isSet("discordAuth")) {
			val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
			try {
				discordAuth.isReady(true)
				userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
			} catch (e: Exception) {
				req.session().unset("discordAuth")
			}
		}

		val type = req.param("guildId").value()

		val server = loritta.serversColl.find(
				Filters.or(
						Filters.and(
								Filters.eq("serverListConfig.enabled", true),
								Filters.eq("serverListConfig.vanityUrl", type)
						),
						Filters.and(
								Filters.eq("serverListConfig.enabled", true),
								Filters.eq("_id", type)
						)
				)
		).firstOrNull()

		if (server == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNKNOWN_GUILD
			return payload.toString()
		}

		val guild = lorittaShards.getGuildById(server.guildId)!!

		val information = JsonObject()
		information["id"] = guild.id
		information["iconUrl"] = guild.iconUrl?.replace("jpg", "png")
		information["invite"] = guild.invites.complete().first { !it.isTemporary }.url
		information["name"] = guild.name
		information["tagline"] = Jsoup.clean(server.serverListConfig.tagline, Whitelist.none())
		information["description"] = Jsoup.clean(server.serverListConfig.description, Whitelist.none())
		information["keywords"] = Loritta.GSON.toJsonTree(server.serverListConfig.keywords)
		information["ownerId"] = guild.owner.user.id
		information["ownerName"] = guild.owner.user.name
		information["ownerDiscriminator"] = guild.owner.user.discriminator
		information["ownerAvatarUrl"] = guild.owner.user.effectiveAvatarUrl.replace("jpg", "png")
		information["memberCount"] = guild.members.size
		information["onlineCount"] = guild.members.count { it.onlineStatus != OnlineStatus.OFFLINE }
		information["serverCreatedAt"] = guild.creationTime.toEpochSecond() * 1000
		information["joinedAt"] = guild.selfMember.joinDate.toEpochSecond() * 1000
		information["hasCustomBackground"] = File(Loritta.FRONTEND, "static/assets/img/servers/backgrounds/${server.guildId}.png").exists()
		information["voteCount"] = server.serverListConfig.votes.size
		information["validVoteCount"] = server.serverListConfig.votes.count { it.votedAt > System.currentTimeMillis() - 2592000000}
		information["canVote"] = true
		// 1 = not logged in
		// 2 = not member
		// 3 = needs to wait more than 1 hour before voting
		// 4 = needs to wait until next day
		if (userIdentification != null) {
			val isMember = guild.getMemberById(userIdentification.id) != null

			if (!isMember) {
				information["canVote"] = false
				information["cantVoteReason"] = 2
			} else {
				val vote = server.serverListConfig.votes
						.lastOrNull { it.id == userIdentification.id }

				if (vote == null) {
					information["canVote"] = true
				} else {
					val votedAt = vote.votedAt

					val calendar = Calendar.getInstance()
					calendar.timeInMillis = votedAt
					calendar.set(Calendar.HOUR_OF_DAY, 0)
					calendar.set(Calendar.MINUTE, 0)
					calendar.add(Calendar.DAY_OF_MONTH, 1)
					val tomorrow = calendar.timeInMillis

					val canVote = System.currentTimeMillis() > tomorrow

					if (canVote) {
						information["canVote"] = true
					} else {
						information["canVote"] = false
						information["cantVoteReason"] = 4
						information["canVoteNext"] = tomorrow
					}
				}
			}
		} else {
			information["canVote"] = false
			information["cantVoteReason"] = 1
		}

		val serverEmotes = JsonArray()

		guild.emotes.forEach {
			val emote = JsonObject()
			emote["name"] = it.name
			emote["imageUrl"] = it.imageUrl
			serverEmotes.add(emote)
		}

		information["serverEmotes"] = serverEmotes

		return information.toString()
	}
}