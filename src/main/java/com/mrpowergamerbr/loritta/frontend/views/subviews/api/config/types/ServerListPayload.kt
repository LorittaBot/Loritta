package com.mrpowergamerbr.loritta.frontend.views.subviews.api.config.types

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaPartner
import net.dv8tion.jda.core.entities.Guild

class ServerListPayload : ConfigPayloadType("server_list") {
	override fun process(payload: JsonObject, serverConfig: ServerConfig, guild: Guild) {
		val serverListConfig = serverConfig.serverListConfig
		serverListConfig.isEnabled = payload["isEnabled"].bool
		serverListConfig.tagline = payload["tagline"].string
		serverListConfig.description = payload["description"].string
		serverListConfig.keywords = payload["keywords"].array.mapNotNull { LorittaPartner.Keyword.valueOf(it.string) }.toMutableList()
		serverListConfig.inviteUrl = payload["inviteUrl"].string
		if (serverListConfig.isPartner || serverListConfig.isSponsored) {
			serverListConfig.vanityUrl = payload["vanityUrl"].string
		}
	}
}