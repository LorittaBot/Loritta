package net.perfectdreams.loritta.morenitta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.common.utils.ActionType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.auditlog.WebAuditLogUtils
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.update

class ResetXpPayload(val loritta: LorittaBot) : ConfigPayloadType("level") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild) {
		runBlocking {
			loritta.pudding.transaction {
				GuildProfiles.update({ GuildProfiles.guildId eq guild.idLong }) {
					it[xp] = 0
				}
			}
		}

		WebAuditLogUtils.addEntry(
			loritta,
			guild.idLong,
			userIdentification.id.toLong(),
			ActionType.RESET_XP,
			jsonObject()
		)
	}
}