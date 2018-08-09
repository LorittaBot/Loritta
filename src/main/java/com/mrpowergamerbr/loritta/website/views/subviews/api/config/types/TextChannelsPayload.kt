package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.nullString
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.userdata.TextChannelConfig
import net.dv8tion.jda.core.entities.Guild

class TextChannelsPayload : ConfigPayloadType("text_channels") {
	override fun process(payload: JsonObject, serverConfig: ServerConfig, guild: Guild) {
		serverConfig.textChannelConfigs.clear()
		val entries = payload["entries"].array

		for (entry in entries) {
			val id = entry["id"].nullString ?: continue

			val config = if (id == "default") {
				// Config default
				val textChannelConfig = TextChannelConfig("default")
				serverConfig.defaultTextChannelConfig = textChannelConfig
				textChannelConfig
			} else {
				val textChannelConfig = TextChannelConfig(id)
				serverConfig.textChannelConfigs.add(textChannelConfig)
				textChannelConfig
			}

			applyReflection(payload, config)
		}
	}
}