package net.perfectdreams.loritta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIGuildAuthRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.website.utils.extensions.trueIp
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class PostSendMessageGuildRoute(loritta: LorittaDiscord) : RequiresAPIGuildAuthRoute(loritta, "/send-message") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		// Rate Limit
		val last = com.mrpowergamerbr.loritta.utils.loritta.apiCooldown.getOrDefault(call.request.trueIp, 0L)

		val diff = System.currentTimeMillis() - last
		if (4000 >= diff)
			throw WebsiteAPIException(
					HttpStatusCode.TooManyRequests,
					com.mrpowergamerbr.loritta.utils.WebsiteUtils.createErrorPayload(
							LoriWebCode.RATE_LIMIT,
							"Rate limit!"
					)
			)

		com.mrpowergamerbr.loritta.utils.loritta.apiCooldown[call.request.trueIp] = System.currentTimeMillis()

		val json = jsonParser.parse(call.receiveText()).obj
		val channelId = json["channelId"].nullString
		val messageString = json["message"].string
		val customTokens = json["customTokens"].nullObj
		val sourceList = json["sources"].nullArray

		val sources = mutableListOf<Any>(guild)

		if (sourceList != null) {
			for (element in sourceList) {
				val str = element.string

				when (str) {
					"user" -> sources.add(lorittaShards.getUserById(userIdentification.id)!!)
					"member" -> {
						val member = guild.getMemberById(userIdentification.id)

						if (member != null)
							sources.add(member)
					}
				}
			}
		}

		val tokens = mutableMapOf<String, String>()

		customTokens?.toMap()?.forEach { key, value ->
			tokens[key] = value.string
		}

		val message = try {
			MessageUtils.generateMessage(messageString, sources, guild, tokens)
		} catch (e: Exception) {
			e.printStackTrace()
			null
		} ?: throw WebsiteAPIException(
						HttpStatusCode.BadRequest,
						com.mrpowergamerbr.loritta.utils.WebsiteUtils.createErrorPayload(
								LoriWebCode.INVALID_MESSAGE,
								"Invalid message"
						)
				)

		if (channelId != null) {
			val channel = guild.getTextChannelById(channelId)
					?: throw WebsiteAPIException(
							HttpStatusCode.BadRequest,
							com.mrpowergamerbr.loritta.utils.WebsiteUtils.createErrorPayload(
									LoriWebCode.CHANNEL_DOESNT_EXIST,
									"Channel ${channelId} doesn't exist"
							)
					)

			if (!channel.canTalk())
				throw WebsiteAPIException(
						HttpStatusCode.BadRequest,
						com.mrpowergamerbr.loritta.utils.WebsiteUtils.createErrorPayload(
								LoriWebCode.CANT_TALK_IN_CHANNEL,
								"Channel ${channelId} doesn't exist"
						)
				)

			val message = channel.sendMessage(message).await()

			call.respondJson(jsonObject("messageId" to message.id), HttpStatusCode.Created)
			return
		} else {
			val user = lorittaShards.getUserById(userIdentification.id) ?: throw WebsiteAPIException(
					HttpStatusCode.BadRequest,
					com.mrpowergamerbr.loritta.utils.WebsiteUtils.createErrorPayload(
							LoriWebCode.MEMBER_DISABLED_DIRECT_MESSAGES,
							"Member ${userIdentification.id} disabled direct messages"
					)
			)

			try {
				val message = user.openPrivateChannel().await().sendMessage(message).await()

				call.respondJson(jsonObject("messageId" to message.id), HttpStatusCode.Created)
				return
			} catch (e: Exception) {
				throw WebsiteAPIException(
						HttpStatusCode.BadRequest,
						com.mrpowergamerbr.loritta.utils.WebsiteUtils.createErrorPayload(
								LoriWebCode.MEMBER_DISABLED_DIRECT_MESSAGES,
								"Member ${userIdentification.id} disabled direct messages"
						)
				)
			}
		}
	}
}