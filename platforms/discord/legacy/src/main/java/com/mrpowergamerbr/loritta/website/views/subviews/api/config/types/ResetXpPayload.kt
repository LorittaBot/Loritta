package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.utils.ActionType
import net.perfectdreams.loritta.utils.auditlog.WebAuditLogUtils
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ResetXpPayload : ConfigPayloadType("level") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild) {
		transaction(Databases.loritta) {
			GuildProfiles.update({ GuildProfiles.guildId eq guild.idLong }) {
				it[xp] = 0
			}
		}

		WebAuditLogUtils.addEntry(
				guild.idLong,
				userIdentification.id.toLong(),
				ActionType.RESET_XP,
				jsonObject()
		)
	}
}