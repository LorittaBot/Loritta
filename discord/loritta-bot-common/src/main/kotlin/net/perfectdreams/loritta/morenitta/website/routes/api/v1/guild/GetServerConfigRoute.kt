package net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild

import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import io.ktor.server.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIGuildAuthRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class GetServerConfigRoute(loritta: LorittaBot) : RequiresAPIGuildAuthRoute(loritta, "/config") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		val serverConfigJson = WebsiteUtils.transformToDashboardConfigurationJson(
				userIdentification,
				guild,
				serverConfig
		)

		call.respondJson(serverConfigJson)
	}
}