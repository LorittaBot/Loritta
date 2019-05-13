package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.bool
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.DonationConfig
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import net.dv8tion.jda.api.entities.Guild
import org.jetbrains.exposed.sql.transactions.transaction

class DailyMultiplierPayload : ConfigPayloadType("daily_multiplier") {
	override fun process(payload: JsonObject, userIdentification: TemmieDiscordAuth.UserIdentification, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, guild: Guild) {
		transaction(Databases.loritta) {
			val donationConfig = serverConfig.donationConfig ?: DonationConfig.new {
                this.dailyMultiplier = false
			}
			donationConfig.dailyMultiplier = payload["dailyMultiplier"].bool

			serverConfig.donationConfig = donationConfig
		}
	}
}