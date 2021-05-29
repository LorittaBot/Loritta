package net.perfectdreams.loritta.website.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml

class ExtrasRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/extras") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val variables = call.legacyVariables(locale)
		variables["extraType"] = "default"

		call.respondHtml(evaluate("extras.html", variables))
	}
}