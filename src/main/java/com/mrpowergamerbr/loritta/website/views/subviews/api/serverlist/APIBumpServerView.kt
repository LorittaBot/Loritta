package com.mrpowergamerbr.loritta.website.views.subviews.api.serverlist

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.save
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.website.views.subviews.api.NoVarsView
import mu.KotlinLogging
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response

class APIBumpServerView : NoVarsView() {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/server-list/bump"))
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)

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

		if (userIdentification == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNAUTHORIZED
			return payload.toString()
		}

		val type = req.param("guildId").value()

		val serverConfig = loritta.serversColl.find(
				Filters.eq("_id", type)
		).firstOrNull()

		if (serverConfig == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNKNOWN_GUILD
			return payload.toString()
		}

		val guild = lorittaShards.getGuildById(type)!!
		if (guild.getMemberById(userIdentification.id) == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.NOT_IN_GUILD
			return payload.toString()
		}

		if (serverConfig.serverListConfig.lastBump + 14_400_000 > System.currentTimeMillis()) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.RATE_LIMITED
			return payload.toString()
		}

		val profile = loritta.getLorittaProfileForUser(userIdentification.id)

		if (750 > profile.dreams) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.INSUFFICIENT_FUNDS
			return payload.toString()
		}

		profile.dreams -= 750

		serverConfig.serverListConfig.lastBump = System.currentTimeMillis()
		loritta save serverConfig
		loritta save profile


		val member = guild.getMemberById(userIdentification.id)

		if (serverConfig.serverListConfig.sendOnPromote && serverConfig.serverListConfig.promoteBroadcastChannelId != null && serverConfig.serverListConfig.promoteBroadcastMessage != null) {
			val textChannel = guild.getTextChannelById(serverConfig.serverListConfig.promoteBroadcastChannelId)

			if (textChannel != null) {
				val customTokens = mutableMapOf<String, String>(
						"vote-count" to serverConfig.serverListConfig.votes.count { it.id == member.user.id }.toString()
				)

				val message = MessageUtils.generateMessage(
						serverConfig.serverListConfig.promoteBroadcastMessage!!,
						listOf(guild, member),
						guild,
						customTokens
				)

				if (message != null)
					textChannel.sendMessage(message).complete()
			}
		}

		val payload = JsonObject()
		payload["api:code"] = LoriWebCodes.SUCCESS
		payload["bumpedAt"] = serverConfig.serverListConfig.lastBump

		logger.info { "${userIdentification.id} promoveu ${serverConfig.guildId}!" }

		return payload.toString()
	}
}