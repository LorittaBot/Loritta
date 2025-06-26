package net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import io.ktor.server.application.*
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIGuildAuthRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class GetServerConfigSectionRoute(
	loritta: LorittaBot,
	val website: LorittaWebsite
) : RequiresAPIGuildAuthRoute(loritta, "/config/{sections}") {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
	}

	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		val sections = call.parameters["sections"]!!.split(",").toSet()

		val payload = jsonObject()

		for (section in sections) {
			val transformer = website.configTransformers.firstOrNull { it.payloadType == section }

			if (transformer != null)
				payload[transformer.configKey] = transformer.toJson(userIdentification, guild, serverConfig)
		}

		logger.info { "Response for ${call.request.trueIp} (guild: ${guild.idLong} / user: ${userIdentification.id}) is $payload" }

		call.respondJson(payload)
	}
}