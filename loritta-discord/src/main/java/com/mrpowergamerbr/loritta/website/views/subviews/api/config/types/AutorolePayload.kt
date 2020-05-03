package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.nullLong
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.dao.servers.moduleconfigs.AutoroleConfig
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.transactions.transaction

class AutorolePayload : ConfigPayloadType("autorole") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, guild: Guild) {
		val isEnabled = payload["isEnabled"].bool
		val giveOnlyAfterMessageWasSent = payload["giveOnlyAfterMessageWasSent"].bool
		val giveRolesAfter = payload["giveRolesAfter"].nullLong
		val roles = payload["roles"].array.map { it.long }.toSet()

		transaction(Databases.loritta) {
			val autoroleConfig = serverConfig.autoroleConfig

			if (!isEnabled) {
				serverConfig.autoroleConfig = null
				autoroleConfig?.delete()
			} else {
				val newConfig = autoroleConfig ?: AutoroleConfig.new {
					this.enabled = isEnabled
					this.giveOnlyAfterMessageWasSent = giveOnlyAfterMessageWasSent
					this.giveRolesAfter = giveRolesAfter
					this.roles = roles.toTypedArray()
				}

				newConfig.enabled = isEnabled
				newConfig.giveOnlyAfterMessageWasSent = giveOnlyAfterMessageWasSent
				newConfig.giveRolesAfter = giveRolesAfter
				newConfig.roles = roles.toTypedArray()

				serverConfig.autoroleConfig = autoroleConfig
			}
		}
	}
}