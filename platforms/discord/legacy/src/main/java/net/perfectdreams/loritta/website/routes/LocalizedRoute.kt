package net.perfectdreams.loritta.website.routes

import io.ktor.application.*
import io.ktor.request.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.utils.extensions.redirect
import net.perfectdreams.sequins.ktor.BaseRoute

abstract class LocalizedRoute(val loritta: LorittaDiscord, val originalPath: String) : BaseRoute("/{localeId}$originalPath") {
	open val isMainClusterOnlyRoute = false

	override suspend fun onRequest(call: ApplicationCall) {
		if (isMainClusterOnlyRoute && !loritta.isMaster)
			// If this is a main cluster only route, we are going to redirect to Loritta's main website
			redirect(loritta.instanceConfig.loritta.website.url.removeSuffix("/") + call.request.path(), true)

		val localeIdFromPath = call.parameters["localeId"]

		// Pegar a locale da URL e, caso não existir, faça fallback para o padrão BR
		val locale = loritta.localeManager.locales.values.firstOrNull { it.path == localeIdFromPath }

		if (locale != null) {
			return onLocalizedRequest(
					call,
					locale
			)
		}
	}

	abstract suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale)

	fun getPathWithoutLocale(call: ApplicationCall) = call.request.path().split("/").drop(2).joinToString("/")
}