package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.transactions.transaction

class GeneralConfigPayload : ConfigPayloadType("default") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild) {
		transaction(Databases.loritta) {
			serverConfig.commandPrefix = payload["commandPrefix"].string
			serverConfig.deleteMessageAfterCommand = payload["deleteMessageAfterCommand"].bool
			serverConfig.warnOnUnknownCommand = payload["warnOnUnknownCommand"].bool
			serverConfig.warnOnMissingPermission = payload["warnOnMissingPermission"].bool
			serverConfig.warnIfBlacklisted = payload["warnIfBlacklisted"].bool
			serverConfig.blacklistedChannels = payload["blacklistedChannels"].array.map { it.long }.toTypedArray()
			serverConfig.blacklistedWarning = payload["blacklistedWarning"].nullString
		}
	}
}