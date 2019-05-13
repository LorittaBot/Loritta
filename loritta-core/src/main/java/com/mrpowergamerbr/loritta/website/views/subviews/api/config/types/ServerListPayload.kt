package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaPartner
import net.dv8tion.jda.api.entities.Guild
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class ServerListPayload : ConfigPayloadType("server_list") {
	override fun process(payload: JsonObject, userIdentification: TemmieDiscordAuth.UserIdentification, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, guild: Guild) {
		val serverListConfig = legacyServerConfig.serverListConfig
		serverListConfig.isEnabled = payload["isEnabled"].bool
		serverListConfig.tagline = payload["tagline"].string
		serverListConfig.description = payload["description"].string
		serverListConfig.keywords = payload["keywords"].array.mapNotNull { LorittaPartner.Keyword.valueOf(it.string) }.toMutableList()
		serverListConfig.sendOnVote = payload["sendOnVote"].bool
		serverListConfig.voteBroadcastChannelId = payload["voteBroadcastChannelId"].string
		serverListConfig.voteBroadcastMessage = payload["voteBroadcastMessage"].string
		serverListConfig.sendOnPromote = payload["sendOnPromote"].bool
		serverListConfig.promoteBroadcastChannelId = payload["promoteBroadcastChannelId"].string
		serverListConfig.promoteBroadcastMessage = payload["promoteBroadcastMessage"].string

		if (serverListConfig.isPartner || serverListConfig.isSponsored) {
			serverListConfig.vanityUrl = payload["vanityUrl"].string
		}

		val data = payload["backgroundImage"].nullString

		if (data != null) {
			val base64Image = data.split(",")[1]
			val imageBytes = Base64.getDecoder().decode(base64Image)
			val img = ImageIO.read(ByteArrayInputStream(imageBytes))

			if (img != null) {
				ImageIO.write(img, "png", File(Loritta.FRONTEND, "static/assets/img/servers/backgrounds/${guild.id}.png"))
			}
		}
	}
}