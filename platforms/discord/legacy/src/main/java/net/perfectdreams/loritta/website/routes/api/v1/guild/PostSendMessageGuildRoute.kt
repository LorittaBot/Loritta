package net.perfectdreams.loritta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullArray
import com.github.salomonbrys.kotson.nullObj
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toMap
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIGuildAuthRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.website.utils.extensions.trueIp
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set

class PostSendMessageGuildRoute(loritta: LorittaDiscord) : RequiresAPIGuildAuthRoute(loritta, "/send-message") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		// Rate Limit
		val last = com.mrpowergamerbr.loritta.utils.loritta.apiCooldown.getOrDefault(call.request.trueIp, 0L)

		val diff = System.currentTimeMillis() - last
		if (4000 >= diff)
			throw WebsiteAPIException(
					HttpStatusCode.TooManyRequests,
					net.perfectdreams.loritta.website.utils.WebsiteUtils.createErrorPayload(
							LoriWebCode.RATE_LIMIT,
							"Rate limit!"
					)
			)

		com.mrpowergamerbr.loritta.utils.loritta.apiCooldown[call.request.trueIp] = System.currentTimeMillis()

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
						WebsiteUtils.createErrorPayload(
								LoriWebCode.INVALID_MESSAGE,
								"Invalid message"
						)
				)

		if (channelId != null) {
			val channel = guild.getTextChannelById(channelId)
					?: throw WebsiteAPIException(
							HttpStatusCode.BadRequest,
							WebsiteUtils.createErrorPayload(
									LoriWebCode.CHANNEL_DOESNT_EXIST,
									"Channel ${channelId} doesn't exist"
							)
					)

			if (!channel.canTalk())
				throw WebsiteAPIException(
						HttpStatusCode.BadRequest,
						WebsiteUtils.createErrorPayload(
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
					WebsiteUtils.createErrorPayload(
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
								LoriWebCode.MEMBER_DISABLED_DIRECT_MESSAGES,
								"Member ${userIdentification.id} disabled direct messages"
						)
				)
			}
		}
	}
}