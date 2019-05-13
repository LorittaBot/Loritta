package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.guild

import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import com.mrpowergamerbr.loritta.website.LoriWebCode
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.Body
import org.jooby.mvc.Local
import org.jooby.mvc.POST
import org.jooby.mvc.Path

@Path("/api/v1/guild/:guildId/send-message")
class SendMessageGuildController {
	private val logger = KotlinLogging.logger {}

	@POST
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.DISCORD_GUILD_REST_AUTH)
	fun sendMessage(req: Request, res: Response, guildId: String, @Local userIdentification: TemmieDiscordAuth.UserIdentification, @Local guild: Guild, @Body rawMessage: String) {
		res.type(MediaType.json)

		// Rate Limit
		val last = loritta.apiCooldown.getOrDefault(req.header("X-Forwarded-For").value(), 0L)

		val diff = System.currentTimeMillis() - last
		if (4000 >= diff) {
			res.send(
					WebsiteUtils.createErrorPayload(
							LoriWebCode.RATE_LIMIT,
							"Rate limit!"
					)
			)
			return
		}

		loritta.apiCooldown[req.header("X-Forwarded-For").value()] = System.currentTimeMillis()

		val json = jsonParser.parse(rawMessage).obj
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
		}

		if (message == null) {
			res.status(Status.BAD_REQUEST)
			res.send(
					WebsiteUtils.createErrorPayload(
							LoriWebCode.INVALID_MESSAGE,
							"Invalid message"
					)
			)
			return
		}

		if (channelId != null) {
			val channel = guild.getTextChannelById(channelId)

			if (channel == null) {
				res.status(Status.BAD_REQUEST)
				res.send(
						WebsiteUtils.createErrorPayload(
								LoriWebCode.CHANNEL_DOESNT_EXIST,
								"Channel ${channelId} doesn't exist"
						)
				)
				return
			}

			if (!channel.canTalk()) {
				res.status(Status.BAD_REQUEST)
				res.send(
						WebsiteUtils.createErrorPayload(
								LoriWebCode.CANT_TALK_IN_CHANNEL,
								"I can't talk in channel ${channelId} due to missing permissions"
						)
				)
				return
			}

			val message = channel.sendMessage(message).complete()

			res.status(Status.CREATED)
			res.send(
					jsonObject("messageId" to message.id)
			)
			return
		} else {
			val user = lorittaShards.getUserById(userIdentification.id)

			if (user == null) {
				res.status(Status.BAD_REQUEST)
				res.send(
						WebsiteUtils.createErrorPayload(
								LoriWebCode.MEMBER_DISABLED_DIRECT_MESSAGES,
								"Member ${userIdentification.id} disabled direct messages"
						)
				)
				return
			}

			try {
				val message = user.openPrivateChannel().complete().sendMessage(message).complete()

				res.status(Status.CREATED)
				res.send(
						jsonObject("messageId" to message.id)
				)
				return
			} catch (e: Exception) {
				res.status(Status.BAD_REQUEST)
				res.send(
						WebsiteUtils.createErrorPayload(
								LoriWebCode.MEMBER_DISABLED_DIRECT_MESSAGES,
								"Member ${userIdentification.id} disabled direct messages"
						)
				)
				return
			}
		}
	}
}