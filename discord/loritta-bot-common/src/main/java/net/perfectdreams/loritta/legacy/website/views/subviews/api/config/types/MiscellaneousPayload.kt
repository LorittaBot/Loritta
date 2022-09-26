package net.perfectdreams.loritta.legacy.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.bool
import com.google.gson.JsonObject
import net.perfectdreams.loritta.legacy.dao.ServerConfig
import net.perfectdreams.loritta.legacy.network.Databases
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.legacy.dao.servers.moduleconfigs.MiscellaneousConfig
import net.perfectdreams.loritta.legacy.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.transactions.transaction

class MiscellaneousPayload : ConfigPayloadType("miscellaneous") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild) {
		val enableQuirky = payload["enableQuirky"].bool
		val enableBomDiaECia = payload["enableBomDiaECia"].bool

		transaction(Databases.loritta) {
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