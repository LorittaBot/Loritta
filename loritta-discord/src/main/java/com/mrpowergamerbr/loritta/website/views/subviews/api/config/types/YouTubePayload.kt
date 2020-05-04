package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class YouTubePayload : ConfigPayloadType("youtube") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild) {
		transaction(Databases.loritta) {
			TrackedYouTubeAccounts.deleteWhere {
				TrackedYouTubeAccounts.guildId eq guild.idLong
			}

			val accounts = payload["accounts"].array

			for (account in accounts) {
				TrackedYouTubeAccounts.insert {
					it[guildId] = guild.idLong
					it[channelId] = account["channel"].long
					it[youTubeChannelId] = account["youTubeChannelId"].string
					it[message] = account["message"].string
				}
			}
		}
	}
}