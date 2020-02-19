package net.perfectdreams.loritta.website.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.ApplicationCall
import io.ktor.request.path
import net.perfectdreams.loritta.platform.discord.LorittaDiscord

abstract class LocalizedRoute(loritta: LorittaDiscord, val originalPath: String) : BaseRoute(loritta, "/{localeId}$originalPath") {
	override suspend fun onRequest(call: ApplicationCall) {
		val localeIdFromPath = call.parameters["localeId"]

		// Pegar a locale da URL e, caso não existir, faça fallback para o padrão BR
		val locale = loritta.locales.values.firstOrNull { it.path == localeIdFromPath }

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