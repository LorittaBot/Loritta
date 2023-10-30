package net.perfectdreams.loritta.morenitta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.bool
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.DonationConfig
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class DailyMultiplierPayload(val loritta: LorittaBot) : ConfigPayloadType("daily_multiplier") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild) {
		runBlocking {
			loritta.pudding.transaction {
				val donationConfig = serverConfig.donationConfig ?: DonationConfig.new {
					this.dailyMultiplier = false
				}
				donationConfig.dailyMultiplier = payload["dailyMultiplier"].bool

				serverConfig.donationConfig = donationConfig
			}
		}
	}
}