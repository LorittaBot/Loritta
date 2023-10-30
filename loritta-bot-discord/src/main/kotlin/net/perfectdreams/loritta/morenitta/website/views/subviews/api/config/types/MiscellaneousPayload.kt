package net.perfectdreams.loritta.morenitta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.bool
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.MiscellaneousConfig
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import org.jetbrains.exposed.sql.transactions.transaction

class MiscellaneousPayload(val loritta: LorittaBot) : ConfigPayloadType("miscellaneous") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild) {
		val enableQuirky = payload["enableQuirky"].bool
		val enableBomDiaECia = payload["enableBomDiaECia"].bool

		runBlocking {
			loritta.pudding.transaction {
				val miscellaneousConfig = serverConfig.miscellaneousConfig

				val newConfig = miscellaneousConfig ?: MiscellaneousConfig.new {
					this.enableQuirky = enableQuirky
					this.enableBomDiaECia = enableBomDiaECia
				}

				newConfig.enableQuirky = enableQuirky
				newConfig.enableBomDiaECia = enableBomDiaECia

				serverConfig.miscellaneousConfig = newConfig
			}
		}
	}
}