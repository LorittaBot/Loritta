package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.listeners.DiscordListener
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.counter.CounterThemes
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.tables.servers.moduleconfigs.MemberCounterChannelConfigs
import net.perfectdreams.loritta.utils.FeatureFlags
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class TextChannelsPayload : ConfigPayloadType("text_channels") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild) {
		transaction(Databases.loritta) {
			MemberCounterChannelConfigs.deleteWhere {
				MemberCounterChannelConfigs.guild eq serverConfig.id
			}
		}

		val entries = payload["entries"].array

		for (entry in entries) {
			val id = entry["id"].nullLong ?: continue

			val obj = entry.obj
			if (obj.has("memberCounterConfig")) {
				val memberCounterConfig = obj["memberCounterConfig"].obj
				val topic = memberCounterConfig["topic"].string
				val theme = memberCounterConfig["theme"].string
				val padding = memberCounterConfig["padding"].int

				transaction(Databases.loritta) {
					MemberCounterChannelConfigs.insert {
						it[MemberCounterChannelConfigs.guild] = serverConfig.id
						it[MemberCounterChannelConfigs.channelId] = id
						it[MemberCounterChannelConfigs.topic] = topic
						it[MemberCounterChannelConfigs.theme] = CounterThemes.valueOf(theme)
						it[MemberCounterChannelConfigs.padding] = padding
					}
				}


				if (FeatureFlags.isEnabled("member-counter-update"))
					DiscordListener.queueTextChannelTopicUpdates(guild, serverConfig, true)
			}
		}
	}
}