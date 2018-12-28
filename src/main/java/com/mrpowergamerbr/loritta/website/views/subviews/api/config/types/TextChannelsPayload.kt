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
		// por enquanto não iremos apagar as configurações atuais
		// para não limpar as coisas de anti spam
		val entries = payload["entries"].array

		serverConfig.textChannelConfigs.clear() // oof anti spam is broken
		for (entry in entries) {
			val id = entry["id"].nullString ?: continue

			val config = if (id == "default") {
				// Config default
				serverConfig.defaultTextChannelConfig
			} else {
				if (serverConfig.hasTextChannelConfig(id)) {
					serverConfig.getTextChannelConfig(id)
				} else {
					val textChannelConfig = TextChannelConfig(id)
					serverConfig.textChannelConfigs.add(textChannelConfig)
					textChannelConfig
				}
			}

			val obj = entry.obj
			if (obj.has("memberCounterConfig")) {
				val memberCounterConfig = obj["memberCounterConfig"].obj
				val topic = memberCounterConfig["topic"].string
				val theme = memberCounterConfig["theme"].string

				config.memberCounterConfig = MemberCounterConfig(
						topic,
						CounterThemeName.valueOf(theme)
				)

				for (textChannel in guild.textChannels) {
					val memberCountConfig = serverConfig.getTextChannelConfig(textChannel).memberCounterConfig ?: continue
					val formattedTopic = memberCountConfig.getFormattedTopic(guild)
					textChannel.manager.setTopic(formattedTopic).queue()
				}
			} else {
				config.memberCounterConfig = null
			}
			// applyReflection(entry.obj, config)
		}
	}
}