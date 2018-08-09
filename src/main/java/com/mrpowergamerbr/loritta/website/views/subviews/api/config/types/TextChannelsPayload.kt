package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.userdata.MemberCounterConfig
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.userdata.TextChannelConfig
import com.mrpowergamerbr.loritta.utils.counter.CounterThemeName
import net.dv8tion.jda.core.entities.Guild

class TextChannelsPayload : ConfigPayloadType("text_channels") {
	override fun process(payload: JsonObject, serverConfig: ServerConfig, guild: Guild) {
		// serverConfig.textChannelConfigs.clear()
		// por enquanto não iremos apagar as configurações atuais
		// para não limpar as coisas de anti spam
		val entries = payload["entries"].array

		for (entry in entries) {
			val id = entry["id"].nullString ?: continue

			val config = if (id == "default") {
				// Config default
				// val textChannelConfig = TextChannelConfig("default")
				// serverConfig.defaultTextChannelConfig = textChannelConfig
				serverConfig.defaultTextChannelConfig
			} else {
				// val textChannelConfig = TextChannelConfig(id)
				// serverConfig.textChannelConfigs.add(textChannelConfig)
				if (serverConfig.hasTextChannelConfig(id)) {
					serverConfig.getTextChannelConfig(id)
				} else {
					val textChannelConfig = TextChannelConfig(id)
					serverConfig.textChannelConfigs.add(textChannelConfig)
					textChannelConfig
				}
			}

			val obj = entry.obj
			if (obj.has("memberCountConfig")) {
				val memberCounterConfig = obj["memberCountConfig"].obj
				val topic = memberCounterConfig["topic"].string

				config.memberCounterConfig = MemberCounterConfig(
						topic,
						CounterThemeName.DEFAULT
				)
			} else {
				config.memberCounterConfig = null
			}
			// applyReflection(entry.obj, config)
		}
	}
}