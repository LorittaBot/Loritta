package net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kotson.jsonObject
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import io.ktor.server.application.*
import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.HoconUtils.decodeFromFile
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import java.io.File

class GetLorittaActionRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/action/{actionType}") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val actionType = call.parameters["actionType"]

		when (actionType) {
			"emotes" -> {
				Emotes.emoteManager?.loadEmotes()
			}
			"locales" -> {
				net.perfectdreams.loritta.morenitta.utils.loritta.localeManager.loadLocales()
				net.perfectdreams.loritta.morenitta.utils.loritta.loadLegacyLocales()
			}
			"website" -> {
				LorittaWebsite.ENGINE.templateCache.invalidateAll()
			}
			"websitekt" -> {
				net.perfectdreams.loritta.morenitta.website.LorittaWebsite.INSTANCE.pathCache.clear()
			}
			"config" -> {
				val file = File(System.getProperty("conf") ?: "./loritta.conf")
				net.perfectdreams.loritta.morenitta.utils.loritta.config = Constants.HOCON.decodeFromFile(file)
				val file2 = File(System.getProperty("discordConf") ?: "./discord.conf")
				net.perfectdreams.loritta.morenitta.utils.loritta.discordConfig = Constants.HOCON.decodeFromFile(file2)
			}
		}

		call.respondJson(jsonObject())
	}
}