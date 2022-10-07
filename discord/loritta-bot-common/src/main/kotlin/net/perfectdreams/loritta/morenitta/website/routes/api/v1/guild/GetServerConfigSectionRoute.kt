package net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import io.ktor.server.application.ApplicationCall
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIGuildAuthRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class GetServerConfigSectionRoute(
	loritta: LorittaBot,
	val website: LorittaWebsite
) : RequiresAPIGuildAuthRoute(loritta, "/config/{sections}") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		val sections = call.parameters["sections"]!!.split(",").toSet()

		val payload = jsonObject()

		for (section in sections) {
			val transformer = website.configTransformers.firstOrNull { it.payloadType == section }

			if (transformer != null)
				payload[transformer.configKey] = transformer.toJson(userIdentification, guild, serverConfig)
		}

		call.respondJson(payload)
	}
}