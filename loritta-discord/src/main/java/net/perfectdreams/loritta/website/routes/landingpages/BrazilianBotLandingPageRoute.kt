package net.perfectdreams.loritta.website.routes.landingpages

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.ApplicationCall
import io.ktor.request.path
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import java.io.File
import kotlin.reflect.full.createType

class BrazilianBotLandingPageRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/discord-bot-brasileiro") {
	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val html = ScriptingUtils.evaluateWebPageFromTemplate(
					File(
							"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/landing_pages/brazilian-bot.kts"
					),
					mapOf(
							"path" to call.request.path().split("/").drop(2).joinToString("/"),
							"websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
							"locale" to ScriptingUtils.WebsiteArgumentType(BaseLocale::class.createType(nullable = false), locale)
					)
			)

		call.respondHtml(html)
	}
}