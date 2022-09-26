package net.perfectdreams.loritta.legacy.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject
import net.perfectdreams.loritta.legacy.dao.ServerConfig
import net.perfectdreams.loritta.legacy.network.Databases
import net.perfectdreams.loritta.legacy.tables.GuildProfiles
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.legacy.utils.ActionType
import net.perfectdreams.loritta.legacy.utils.auditlog.WebAuditLogUtils
import net.perfectdreams.loritta.legacy.website.session.LorittaJsonWebSession
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