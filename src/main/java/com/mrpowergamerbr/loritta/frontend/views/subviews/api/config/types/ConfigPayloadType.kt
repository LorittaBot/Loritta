package com.mrpowergamerbr.loritta.frontend.views.subviews.api.config.types

import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import net.dv8tion.jda.core.entities.Guild

abstract class ConfigPayloadType(val type: String) {
	abstract fun process(payload: JsonObject, serverConfig: ServerConfig, guild: Guild)
}