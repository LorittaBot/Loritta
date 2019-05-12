package com.mrpowergamerbr.loritta.website.views.subviews.configure

import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.oauth2.SimpleUserIdentification
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Guild
import org.jooby.Request
import org.jooby.Response

class TestMessageView : ConfigureView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, path, variables)
		return path.matches(Regex("^/dashboard/configure/[0-9]+/testmessage"))
	}

	override fun renderConfiguration(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: MongoServerConfig): String {
		val receivedPayload = jsonParser.parse(req.body().value()).obj
		val type = receivedPayload["type"].string
		val content = receivedPayload["content"].string

		val response = JsonObject()
		var textChannelId: String? = null

		if (type == "joinMessage" || type == "leaveMessage" || type == "banMessage" || type == "kickMessage" || type == "livestreamMessage")
			textChannelId = receivedPayload["textChannelId"].string

		var message = content
		val userIdentification = req.ifGet<SimpleUserIdentification>("userIdentification").get()
		val member = guild.getMemberById(userIdentification.id)
		val nickname = member?.effectiveName

		val customTokens = mutableMapOf<String, String>()

		customTokens.put("@user", "<@${userIdentification.id}>")
		customTokens.put("user", userIdentification.username)
		customTokens.put("user-discriminator", userIdentification.discriminator)
		customTokens.put("user-id", userIdentification.id)
		customTokens.put("userAvatarUrl", "https://cdn.discordapp.com/avatars/${userIdentification.id}/${userIdentification.avatar}.png")
		customTokens.put("nickname", nickname ?: "???")
		customTokens.put("guild", guild.name)
		customTokens.put("guildsize", guild.members.size.toString())
		customTokens.put("@owner", guild.owner?.asMention ?: "???")
		customTokens.put("owner", guild.owner?.effectiveName ?: "???")
		customTokens.put("@staff", "<@${loritta.discordConfig.discord.clientId}>")
		customTokens.put("staff", "Loritta")
		customTokens.put("reason", "You gonna have a bad time.")

		if (type == "livestreamMessage") {
			message = content
			val channelUrl = receivedPayload["channelUrl"].string
			val channelUserLogin = channelUrl.split("/").last()

			val displayName = runBlocking { loritta.twitch.getUserLogin(channelUserLogin) }?.displayName

			if (displayName == null) {
				response["error"] = "Canal inválido"
				return response.toString()
			}

			customTokens.put("game", "Loritta and the Discord's Curse")
			customTokens.put("streamer", displayName)
			customTokens.put("link", "https://www.twitch.tv/$channelUserLogin")
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

			textChannel.sendMessage(MessageUtils.generateMessage(message, null, guild, customTokens)!!).queue()
			response["success"] = true
		} else {
			try {
				member?.user?.openPrivateChannel()?.queue {
					it.sendMessage(MessageUtils.generateMessage(message, null, guild, customTokens)!!).queue()
				}
			} catch (e: Exception) {
				response["error"] = "Sua DM está desativada"
			}
		}

		return response.toString()
	}
}