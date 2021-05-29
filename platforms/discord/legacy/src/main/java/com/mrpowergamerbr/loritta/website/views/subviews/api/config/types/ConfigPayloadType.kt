package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession

abstract class ConfigPayloadType(val type: String) {
	abstract fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild)
}