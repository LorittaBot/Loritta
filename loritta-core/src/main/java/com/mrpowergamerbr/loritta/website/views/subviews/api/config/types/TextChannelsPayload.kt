package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.listeners.DiscordListener
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.MemberCounterConfig
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.userdata.TextChannelConfig
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.counter.CounterThemes
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.utils.FeatureFlags
import org.jooby.Status

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
						CounterThemes.valueOf(theme)
				).apply {
					this.padding = padding

					if (theme == "CUSTOM") {
						val emojiJsonArray = memberCounterConfig["emojis"].nullArray
						if (emojiJsonArray == null || emojiJsonArray.size() != 10)
							throw WebsiteAPIException(Status.UNPROCESSABLE_ENTITY,
									WebsiteUtils.createErrorPayload(
											LoriWebCode.UNAUTHORIZED,
											"Emojis array is null or doesn't have 10 elements!"
									)
							)

						this.emojis = memberCounterConfig["emojis"].array.map { it.string }
					}
				}

				if (FeatureFlags.isEnabled("member-counter-update"))
					DiscordListener.queueTextChannelTopicUpdates(guild, legacyServerConfig, true)
			} else {
				config.memberCounterConfig = null
			}
			// applyReflection(entry.obj, config)
		}
	}
}