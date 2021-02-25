package net.perfectdreams.loritta.plugin.parallaxroutes.routes

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.nullObj
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxEmbed
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.MessageBuilder
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class PostMessageRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/parallax/channels/{channelId}/messages") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		try {
			val json = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()).obj }
			val content = json["content"].string
			val embed = json["embed"].nullObj

			val messageBuilder = MessageBuilder()
					.setContent(content)

			if (embed != null) {
				val parallaxEmbed = gson.fromJson<ParallaxEmbed>(embed)
				messageBuilder.setEmbed(parallaxEmbed.toDiscordEmbed(true))
			}

			val textChannel = lorittaShards.getTextChannelById(call.parameters["channelId"]!!)!!
			val message = textChannel.sendMessage(messageBuilder.build()).complete()

			call.respondJson(ParallaxUtils.transformToJson(message))
		} catch (e: Throwable) {
			e.printStackTrace()
		}
	}
}