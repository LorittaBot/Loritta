package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.guild

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.tables.AuditLog
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path

@Path("/api/v1/guild/:guildId/audit-log")
class GuildWebAuditLogController {
	@GET
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.DISCORD_GUILD_REST_AUTH)
	fun getConfig(req: Request, res: Response, guildId: String, @Local userIdentification: TemmieDiscordAuth.UserIdentification, @Local serverConfig: MongoServerConfig, @Local guild: Guild) {
		res.type(MediaType.json)

		val wrapper = jsonObject()
		val users = jsonArray()
		val entries = jsonArray()

		val auditEntries = transaction(Databases.loritta) {
			AuditLog.select {
				AuditLog.guildId eq guildId.toLong()
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
			val user = runBlocking { lorittaShards.retrieveUserById(userId) } ?: continue

			users.add(
					WebsiteUtils.transformToJson(user)
			)
		}

		wrapper["users"] = users
		wrapper["entries"] = entries

		res.send(wrapper)
	}
}