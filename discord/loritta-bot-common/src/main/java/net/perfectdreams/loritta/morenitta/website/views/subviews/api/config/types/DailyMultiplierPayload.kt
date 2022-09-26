package net.perfectdreams.loritta.morenitta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.bool
import com.google.gson.JsonObject
import net.perfectdreams.loritta.morenitta.dao.DonationConfig
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.network.Databases
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.transactions.transaction

class DailyMultiplierPayload : ConfigPayloadType("daily_multiplier") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild) {
		transaction(Databases.loritta) {
			val donationConfig = serverConfig.donationConfig ?: DonationConfig.new {
                this.dailyMultiplier = false
			}
			donationConfig.dailyMultiplier = payload["dailyMultiplier"].bool

			serverConfig.donationConfig = donationConfig
		}
	}
}