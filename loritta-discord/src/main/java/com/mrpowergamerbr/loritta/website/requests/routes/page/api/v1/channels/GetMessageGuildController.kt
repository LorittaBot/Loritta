package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.channels

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.*
import mu.KotlinLogging
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/channels/:channelId/messages/:messageId")
class GetMessageGuildController {
	private val logger = KotlinLogging.logger {}

	@GET
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.DISCORD_CHANNEL_REST_AUTH)
	fun sendMessage(req: Request, res: Response, channelId: String, messageId: String) {
		res.type(MediaType.json)

		println("Channel ID is $channelId")
		println("Message ID is $messageId")

		val channel = lorittaShards.getTextChannelById(channelId) ?: throw WebsiteAPIException(Status.NOT_FOUND,
				WebsiteUtils.createErrorPayload(
						LoriWebCode.CHANNEL_DOESNT_EXIST,
						"Channel doesn't exist or guild isn't loaded yet"
				)
		)

		println("Channel obj is $channel")

		val message = channel.retrieveMessageById(messageId).complete() ?: throw WebsiteAPIException(Status.NOT_FOUND,
				WebsiteUtils.createErrorPayload(
						LoriWebCode.MESSAGE_DOESNT_EXIST,
						"Message doesn't exist or guild isn't loaded yet"
				)
		)

		println("Message obj is $message")

		res.send(
				gson.toJson(
						jsonObject(
								"id" to message.id,
								"channelId" to message.channel.id,
								"content" to message.contentRaw,
								"reactions" to gson.toJsonTree(message.reactions.map {
									jsonObject(
											"isDiscordEmote" to it.reactionEmote.isEmote,
											"name" to it.reactionEmote.name,
											"id" to if (it.reactionEmote.isEmote) {
												it.reactionEmote.emote.id
											} else null
									)
								}
								)
						)
				)
		)
	}
}