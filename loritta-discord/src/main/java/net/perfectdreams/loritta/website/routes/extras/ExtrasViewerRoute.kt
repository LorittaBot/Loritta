package net.perfectdreams.loritta.website.routes.extras

import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import java.io.File

class ExtrasViewerRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/extras/{pageId}") {
	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val extraType = call.parameters["pageId"]?.replace(".", "")?.replace("/", "")

		if (extraType != null) {
			if (File(LorittaWebsite.FOLDER, "extras/$extraType.html").exists()) {
				val variables = call.legacyVariables(locale)
				variables["extraType"] = extraType
				call.respondHtml(evaluate("extras/$extraType.html", variables))
			}
		}
	}
}