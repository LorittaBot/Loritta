package net.perfectdreams.loritta.website.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import java.io.File
import kotlin.reflect.full.createType

class SupportRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/support") {
	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val html = ScriptingUtils.evaluateWebPageFromTemplate(
				File(
						"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/support.kts"
				),
				mapOf(
						"path" to getPathWithoutLocale(call),
						"websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
						"locale" to ScriptingUtils.WebsiteArgumentType(BaseLocale::class.createType(nullable = false), locale)
				)
		)

		call.respondHtml(html)
	}
}