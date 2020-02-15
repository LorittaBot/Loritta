package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import net.dv8tion.jda.api.entities.Guild

class EconomyPayload : ConfigPayloadType("economy") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, guild: Guild) {
		// applyReflection(payload, legacyServerConfig.economyConfig)
	}
}