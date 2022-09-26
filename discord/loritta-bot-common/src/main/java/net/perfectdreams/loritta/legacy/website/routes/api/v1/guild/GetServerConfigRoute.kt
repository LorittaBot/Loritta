package net.perfectdreams.loritta.legacy.website.routes.api.v1.guild

import net.perfectdreams.loritta.legacy.dao.ServerConfig
import io.ktor.server.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.website.routes.api.v1.RequiresAPIGuildAuthRoute
import net.perfectdreams.loritta.legacy.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.legacy.website.utils.WebsiteUtils
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class GetServerConfigRoute(loritta: LorittaDiscord) : RequiresAPIGuildAuthRoute(loritta, "/config") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		val serverConfigJson = WebsiteUtils.transformToDashboardConfigurationJson(
				userIdentification,
				guild,
				serverConfig
		)

		call.respondJson(serverConfigJson)
	}
}