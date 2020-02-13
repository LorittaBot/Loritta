package com.mrpowergamerbr.loritta.website.requests.routes.page.extras

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.evaluate
import net.perfectdreams.loritta.tables.BlacklistedUsers
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path
import java.io.File

@Path("/:localeId/extras/:pageId")
class ExtrasViewerController {
	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>) {
		val extraType = req.path().split("/").getOrNull(3)?.replace(".", "")?.replace("/", "")

		if (extraType != null) {
			if (File(LorittaWebsite.FOLDER, "extras/$extraType.html").exists()) {
				variables["extraType"] = extraType
				if (extraType == "banned-users") {
					val bannedUsers = transaction(Databases.loritta) {
						Profiles.select {
							Profiles.isBanned eq true
						}.toMutableSet()
					}

					var html = ""
					for (profile in bannedUsers) {
						val userId = profile[Profiles.id].value
						val banReason = profile[Profiles.bannedReason] ?: "???"
						val user = try {
							lorittaShards.getUserById(userId)
						} catch (e: Exception) {
							null
						}

						html += """
							<tr>
							<td>${user?.id ?: userId}</td>
							<td>${if (user != null) "${user.name}#${user.discriminator}" else "???"}</td>
							<td>$banReason</td>
							</tr>
						""".trimIndent()
					}

					variables["tableContents"] = html
				}
				if (extraType == "network-bans") {
					var html = ""
					val bannedUsers = transaction(Databases.loritta) {
						BlacklistedUsers.selectAll().toMutableList()
					}

					for (entry in bannedUsers) {
						val userId = entry[BlacklistedUsers.id].value
						val banReason = entry[BlacklistedUsers.reason]
						val guildId = entry[BlacklistedUsers.guildId]
						val type = entry[BlacklistedUsers.type]

						val user = try {
							lorittaShards.getUserById(userId)
						} catch (e: Exception) {
							null
						}

						val guildName = if (guildId != null) {
							lorittaShards.getGuildById(guildId)?.name ?: guildId
						} else {
							"✘"
						}

						html += """
							<tr>
							<td>${user?.id ?: userId}</td>
							<td>${if (user != null) "${user.name}#${user.discriminator}" else "???"}</td>
							<td>$guildName</td>
							<td>${type}</td>
							<td>$banReason</td>
							</tr>
						""".trimIndent()
					}

					variables["tableContents"] = html
				}
				return res.send(evaluate("extras/$extraType.html", variables))
			}
		}

		res.status(404)
		return res.send(evaluate("404.html"))
	}
}