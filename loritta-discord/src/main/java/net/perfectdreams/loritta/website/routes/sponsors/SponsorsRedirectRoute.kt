package net.perfectdreams.loritta.website.routes.sponsors

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.ApplicationCall
import io.ktor.request.path
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.Sponsor
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import java.io.File
import kotlin.reflect.full.createType

class SponsorsRedirectRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/sponsor/{sponsorSlug}") {
	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val sponsorSlug = call.parameters["sponsorSlug"]

		val sponsor = com.mrpowergamerbr.loritta.utils.loritta.sponsors.firstOrNull { it.slug == sponsorSlug } ?: return

		val html = ScriptingUtils.evaluateWebPageFromTemplate(
				File(
						"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/sponsor_redirect.kts"
				),
				mapOf(
						"path" to call.request.path().split("/").drop(2).joinToString("/"),
						"websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
						"locale" to ScriptingUtils.WebsiteArgumentType(BaseLocale::class.createType(nullable = false), locale),
						"sponsor" to ScriptingUtils.WebsiteArgumentType(Sponsor::class.createType(nullable = false), sponsor)
				)
		)

		call.respondHtml(html)
	}
}