package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaPartner
import net.dv8tion.jda.core.entities.Guild
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO

class ServerListPayload : ConfigPayloadType("server_list") {
	override fun process(payload: JsonObject, serverConfig: ServerConfig, guild: Guild) {
		val serverListConfig = serverConfig.serverListConfig
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
			val imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image)
			val img = ImageIO.read(ByteArrayInputStream(imageBytes))

			if (img != null) {
				ImageIO.write(img, "png", File(Loritta.FRONTEND, "static/assets/img/servers/backgrounds/${guild.id}.png"))
			}
		}
	}
}