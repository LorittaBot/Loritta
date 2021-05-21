package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import io.ktor.application.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.HoconUtils.decodeFromFile
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import java.io.File

class GetLorittaActionRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/action/{actionType}") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val actionType = call.parameters["actionType"]

		when (actionType) {
			"emotes" -> {
				Emotes.emoteManager?.loadEmotes()
			}
			"locales" -> {
				com.mrpowergamerbr.loritta.utils.loritta.localeManager.loadLocales()
				com.mrpowergamerbr.loritta.utils.loritta.loadLegacyLocales()
			}
			"website" -> {
				LorittaWebsite.ENGINE.templateCache.invalidateAll()
			}
			"websitekt" -> {
				net.perfectdreams.loritta.website.LorittaWebsite.INSTANCE.pathCache.clear()
			}
			"config" -> {
				val file = File(System.getProperty("conf") ?: "./loritta.conf")
				com.mrpowergamerbr.loritta.utils.loritta.config = Constants.HOCON.decodeFromFile(file)
				val file2 = File(System.getProperty("discordConf") ?: "./discord.conf")
				com.mrpowergamerbr.loritta.utils.loritta.discordConfig = Constants.HOCON.decodeFromFile(file2)
			}
		}

		call.respondJson(jsonObject())
	}
}