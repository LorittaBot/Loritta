package net.perfectdreams.loritta.website.routes.dashboard.configure

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class ConfigureAutoroleRoute(loritta: LorittaDiscord) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/autorole") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild) {
		loritta as Loritta
		val variables = call.legacyVariables(locale)
		variables["saveType"] = "autorole"

		val serverConfig = loritta.getServerConfigForGuild(guild.id)

		serverConfig.autoroleConfig.roles = serverConfig.autoroleConfig.roles.filter {
			try {
				guild.getRoleById(it) != null
			} catch (e: Exception) {
				false
			}
		}.toMutableList()
		variables["currentAutoroles"] = serverConfig.autoroleConfig.roles.joinToString(separator = ";")

		call.respondHtml(evaluate("autorole.html", variables))
	}
}