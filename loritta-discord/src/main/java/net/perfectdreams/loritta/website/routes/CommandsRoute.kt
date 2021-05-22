package net.perfectdreams.loritta.website.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.application.*
import io.ktor.response.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord

class CommandsRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/commands") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		call.respondText("Se você está vendo isso, quer dizer que alguma configuração deu problema!")
	}
}