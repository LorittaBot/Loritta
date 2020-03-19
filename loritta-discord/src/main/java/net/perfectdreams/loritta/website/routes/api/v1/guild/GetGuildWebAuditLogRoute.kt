package net.perfectdreams.loritta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.AuditLog
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIGuildAuthRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class GetGuildWebAuditLogRoute(loritta: LorittaDiscord) : RequiresAPIGuildAuthRoute(loritta, "/audit-log") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig) {
		val guildId = guild.idLong

		val wrapper = jsonObject()
		val users = jsonArray()
		val entries = jsonArray()

		val auditEntries = transaction(Databases.loritta) {
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
			val user = lorittaShards.retrieveUserInfoById(userId) ?: continue

			users.add(
					com.mrpowergamerbr.loritta.utils.WebsiteUtils.transformToJson(user)
			)
		}

		wrapper["users"] = users
		wrapper["entries"] = entries

		call.respondJson(wrapper)
	}
}