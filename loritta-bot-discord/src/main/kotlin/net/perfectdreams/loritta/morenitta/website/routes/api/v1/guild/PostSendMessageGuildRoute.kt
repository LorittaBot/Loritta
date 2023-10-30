package net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullArray
import com.github.salomonbrys.kotson.nullObj
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toMap
import com.google.gson.JsonParser
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIGuildAuthRoute
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set

class PostSendMessageGuildRoute(loritta: LorittaBot) : RequiresAPIGuildAuthRoute(loritta, "/send-message") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		// Rate Limit
		val last = loritta.apiCooldown.getOrDefault(call.request.trueIp, 0L)

		val diff = System.currentTimeMillis() - last
		if (4000 >= diff)
			throw WebsiteAPIException(
					HttpStatusCode.TooManyRequests,
					net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils.createErrorPayload(
							loritta,
							LoriWebCode.RATE_LIMIT,
							"Rate limit!"
					)
			)

		loritta.apiCooldown[call.request.trueIp] = System.currentTimeMillis()

		val json = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()).obj }
		val channelId = json["channelId"].nullString
		val messageString = json["message"].string
		val customTokens = json["customTokens"].nullObj
		val sourceList = json["sources"].nullArray

		val sources = mutableListOf<Any>(guild)

		if (sourceList != null) {
			for (element in sourceList) {
				val str = element.string

				when (str) {
					"user" -> sources.add(loritta.lorittaShards.getUserById(userIdentification.id)!!)
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
						WebsiteUtils.createErrorPayload(
								loritta,
								LoriWebCode.INVALID_MESSAGE,
								"Invalid message"
						)
				)

		if (channelId != null) {
			val channel = guild.getGuildMessageChannelById(channelId)
					?: throw WebsiteAPIException(
							HttpStatusCode.BadRequest,
							WebsiteUtils.createErrorPayload(
									loritta,
									LoriWebCode.CHANNEL_DOESNT_EXIST,
									"Channel ${channelId} doesn't exist"
							)
					)

			if (!channel.canTalk())
				throw WebsiteAPIException(
						HttpStatusCode.BadRequest,
						WebsiteUtils.createErrorPayload(
								loritta,
								LoriWebCode.CANT_TALK_IN_CHANNEL,
								"Channel ${channelId} doesn't exist"
						)
				)

			val message = channel.sendMessage(message).await()

			call.respondJson(jsonObject("messageId" to message.id), HttpStatusCode.Created)
			return
		} else {
			val user = loritta.lorittaShards.getUserById(userIdentification.id) ?: throw WebsiteAPIException(
					HttpStatusCode.BadRequest,
					WebsiteUtils.createErrorPayload(
							loritta,
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
						WebsiteUtils.createErrorPayload(
								loritta,
								LoriWebCode.MEMBER_DISABLED_DIRECT_MESSAGES,
								"Member ${userIdentification.id} disabled direct messages"
						)
				)
			}
		}
	}
}