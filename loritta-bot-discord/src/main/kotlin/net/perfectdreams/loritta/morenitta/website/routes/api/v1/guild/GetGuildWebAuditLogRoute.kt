package net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import io.ktor.server.application.ApplicationCall
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.pudding.tables.AuditLog
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIGuildAuthRoute
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select

class GetGuildWebAuditLogRoute(loritta: LorittaBot) : RequiresAPIGuildAuthRoute(loritta, "/audit-log") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		val guildId = guild.idLong

		val wrapper = jsonObject()
		val users = jsonArray()
		val entries = jsonArray()

		val auditEntries = loritta.newSuspendedTransaction {
			AuditLog.select {
				AuditLog.guildId eq guildId
			}.orderBy(AuditLog.executedAt, SortOrder.DESC)
					.toMutableList()
		}

		for (entry in auditEntries) {
			entries.add(
					jsonObject(
							"id" to entry[AuditLog.userId],
							"executedAt" to entry[AuditLog.executedAt],
							"type" to entry[AuditLog.actionType].toString(),
							"params" to entry[AuditLog.params]
					)
			)
		}

		val usersInAuditLog = auditEntries.map { it[AuditLog.userId] }.distinct()

		for (userId in usersInAuditLog) {
			KotlinLogging.logger {}.info { "GetGuildWebAuditLogRoute#retrieveUserInfoById - UserId: ${userId}" }
			val user = loritta.lorittaShards.retrieveUserInfoById(userId) ?: continue

			users.add(
					net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils.transformToJson(user)
			)
		}

		wrapper["users"] = users
		wrapper["entries"] = entries

		call.respondJson(wrapper)
	}
}