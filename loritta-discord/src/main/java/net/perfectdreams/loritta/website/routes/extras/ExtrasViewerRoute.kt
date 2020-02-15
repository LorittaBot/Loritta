package net.perfectdreams.loritta.website.routes.extras

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.BlacklistedUsers
import net.perfectdreams.loritta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class ExtrasViewerRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/extras/{pageId}") {
	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val extraType = call.parameters["pageId"]?.replace(".", "")?.replace("/", "")

		if (extraType != null) {
			if (File(LorittaWebsite.FOLDER, "extras/$extraType.html").exists()) {
				val variables = call.legacyVariables(locale)
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

				call.respondHtml(evaluate("extras/$extraType.html", variables))
			}
		}
	}
}