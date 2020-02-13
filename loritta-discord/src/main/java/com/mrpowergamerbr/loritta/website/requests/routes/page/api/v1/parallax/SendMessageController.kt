package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.parallax

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.nullObj
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxEmbed
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import net.dv8tion.jda.api.MessageBuilder
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.Body
import org.jooby.mvc.POST
import org.jooby.mvc.Path

@Path("/api/v1/parallax/channels/:channelId/messages")
class SendMessageController {
	@POST
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response, channelId: String, @Body body: String) {
		res.type(MediaType.json)

		try {
			val json = jsonParser.parse(body).obj
			val content = json["content"].string
			val embed = json["embed"].nullObj

			val messageBuilder = MessageBuilder()
					.setContent(content)

			if (embed != null) {
				val parallaxEmbed = gson.fromJson<ParallaxEmbed>(embed)
				messageBuilder.setEmbed(parallaxEmbed.toDiscordEmbed(true))
			}

			val textChannel = lorittaShards.getTextChannelById(channelId)!!
			val message = textChannel.sendMessage(messageBuilder.build()).complete()

			res.send(
					gson.toJson(
							ParallaxUtils.transformToJson(message)
					)
			)
		} catch (e: Throwable) {
			e.printStackTrace()
		}
	}
}