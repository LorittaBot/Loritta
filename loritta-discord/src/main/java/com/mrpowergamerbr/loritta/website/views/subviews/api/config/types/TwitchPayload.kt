package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class TwitchPayload : ConfigPayloadType("twitch") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, guild: Guild) {
		transaction(Databases.loritta) {
			TrackedTwitchAccounts.deleteWhere {
				TrackedTwitchAccounts.guildId eq guild.idLong
			}

			val accounts = payload["accounts"].array

			for (account in accounts) {
				TrackedTwitchAccounts.insert {
					it[guildId] = guild.idLong
					it[channelId] = account["channel"].long
					it[twitchUserId] = account["twitchUserId"].long
					it[message] = account["message"].string
				}
			}
		}
	}
}