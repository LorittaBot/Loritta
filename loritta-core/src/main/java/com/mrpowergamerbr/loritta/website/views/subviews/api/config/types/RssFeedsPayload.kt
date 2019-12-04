package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.tables.TrackedRssFeeds
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class RssFeedsPayload : ConfigPayloadType("rss_feeds") {
	override fun process(payload: JsonObject, userIdentification: TemmieDiscordAuth.UserIdentification, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, guild: Guild) {
		transaction(Databases.loritta) {
			// Deletar todas que já existem
			TrackedRssFeeds.deleteWhere {
				TrackedRssFeeds.guildId eq guild.idLong
			}

			val announcements = payload["rssFeeds"].array

			// E reinserir!
			announcements.forEach {
				TrackedRssFeeds.insert {
					it[feedUrl] = announcements["feedUrl"].string
					it[channelId] = announcements["channelId"].long
					it[message] = announcements["message"].string
				}
			}
		}
	}
}