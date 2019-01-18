package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.MemberCounterConfig
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.userdata.TextChannelConfig
import com.mrpowergamerbr.loritta.utils.counter.CounterThemeName
import net.dv8tion.jda.core.entities.Guild

class TextChannelsPayload : ConfigPayloadType("text_channels") {
	override fun process(payload: JsonObject, userIdentification: TemmieDiscordAuth.UserIdentification, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, guild: Guild) {
		// por enquanto não iremos apagar as configurações atuais
		// para não limpar as coisas de anti spam
		val entries = payload["entries"].array

		legacyServerConfig.textChannelConfigs.clear() // oof anti spam is broken
		for (entry in entries) {
			val id = entry["id"].nullString ?: continue

			val config = if (id == "default") {
				// Config default
				legacyServerConfig.defaultTextChannelConfig
			} else {
				if (legacyServerConfig.hasTextChannelConfig(id)) {
					legacyServerConfig.getTextChannelConfig(id)
				} else {
					val textChannelConfig = TextChannelConfig(id)
					legacyServerConfig.textChannelConfigs.add(textChannelConfig)
					textChannelConfig
				}
			}

			val obj = entry.obj
			if (obj.has("memberCounterConfig")) {
				val memberCounterConfig = obj["memberCounterConfig"].obj
				val topic = memberCounterConfig["topic"].string
				val theme = memberCounterConfig["theme"].string
				val padding = memberCounterConfig["padding"].int

				config.memberCounterConfig = MemberCounterConfig(
						topic,
						CounterThemeName.valueOf(theme)
				).apply {
					this.padding = padding
				}

				for (textChannel in guild.textChannels) {
					val memberCountConfig = legacyServerConfig.getTextChannelConfig(textChannel).memberCounterConfig ?: continue
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