package net.perfectdreams.loritta.morenitta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.network.Databases
import net.perfectdreams.loritta.morenitta.tables.GuildProfiles
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.utils.ActionType
import net.perfectdreams.loritta.morenitta.utils.auditlog.WebAuditLogUtils
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
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