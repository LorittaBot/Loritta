package com.mrpowergamerbr.loritta.frontend.views.subviews.configure

import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.threads.NewLivestreamThread
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.core.entities.Guild
import org.jooby.Request
import org.jooby.Response

class TestMessageView : ConfigureView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, variables)
		return req.path().matches(Regex("^/dashboard/configure/[0-9]+/testmessage"))
	}

	override fun renderConfiguration(req: Request, res: Response, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: ServerConfig): String {
		val receivedPayload = JSON_PARSER.parse(req.body().value()).obj
		val type = receivedPayload["type"].string
		val content = receivedPayload["content"].string

		val response = JsonObject()
		var textChannelId: String? = null

		if (type == "joinMessage" || type == "leaveMessage" || type == "banMessage" || type == "kickMessage" || type == "livestreamMessage")
			textChannelId = receivedPayload["textChannelId"].string

		var message = content
		val userIdentification = discordAuth.getUserIdentification()
		val member = guild.getMemberById(userIdentification.id)
		val nickname = member.effectiveName
		message = message.replace("{@user}", "<@${userIdentification.id}>")
		message = message.replace("{user}", userIdentification.username)
		message = message.replace("{nickname}", nickname)
		message = message.replace("{guild}", guild.name)
		message = message.replace("{guildsize}", guild.members.size.toString())
		message = message.replace("{@owner}", guild.owner.asMention)
		message = message.replace("{owner}", guild.owner.effectiveName)
		message = message.replace("{@staff}", "<@${Loritta.config.clientId}>")
		message = message.replace("{staff}", "Loritta")
		message = message.replace("{reason}", "You gonna have a bad time.")

		if (type == "livestreamMessage") {
			message = content
			val channelUrl = receivedPayload["channelUrl"].string
			val channelUserLogin = channelUrl.split("/").last()

			val displayName = NewLivestreamThread.getUserDisplayName(channelUserLogin)

			if (displayName == null) {
				response["error"] = "Canal inválido"
				return response.toString()
			}

			message = message.replace("{game}", "Loritta and the Discord's Curse")
			message = message.replace("{streamer}", displayName)
			message = message.replace("{link}", "https://www.twitch.tv/$channelUserLogin")
		}

		if (textChannelId != null) {
			val textChannel = guild.getTextChannelById(textChannelId)

			if (textChannel == null) {
				response["error"] = "Canal de texto inválido"
				return response.toString()
			}

			if (!textChannel.canTalk()) {
				response["error"] = "Sem permissão para falar no canal #${textChannel.name}"
				return response.toString()
			}

			textChannel.sendMessage(message).complete()
			response["success"] = true
		} else {
			try {
				member.user.openPrivateChannel().complete().sendMessage(message).complete()
			} catch (e: Exception) {
				response["error"] = "Sua DM está desativada"
			}
		}

		return response.toString()
	}
}