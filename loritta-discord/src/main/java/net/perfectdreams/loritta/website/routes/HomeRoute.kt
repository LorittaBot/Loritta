package net.perfectdreams.loritta.website.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.request.path
import io.ktor.response.respondText
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.RouteKey
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import java.io.File

class HomeRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		call.respondHtml(
				LorittaWebsite.INSTANCE.pageProvider.render(
						RouteKey.HOME,
						listOf(
								getPathWithoutLocale(call),
								locale
						)
				)
		)
	}
}