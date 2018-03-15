package com.mrpowergamerbr.loritta.frontend.views.subviews.api.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaPartner
import net.dv8tion.jda.core.entities.Guild
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO

class AutorolePayload : ConfigPayloadType("autorole") {
	override fun process(payload: JsonObject, serverConfig: ServerConfig, guild: Guild) {
		val autoroleConfig = serverConfig.autoroleConfig
		autoroleConfig.isEnabled = payload["isEnabled"].bool
		autoroleConfig.roles = payload["roles"].array.map { it.string }.toMutableList()
		autoroleConfig.rolesVoteRewards = Gson().fromJson(payload["rolesVoteRewards"])
	}
}