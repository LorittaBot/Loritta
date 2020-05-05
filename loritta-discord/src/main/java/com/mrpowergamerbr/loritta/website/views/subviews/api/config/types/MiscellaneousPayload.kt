package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.bool
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.dao.servers.moduleconfigs.MiscellaneousConfig
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
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