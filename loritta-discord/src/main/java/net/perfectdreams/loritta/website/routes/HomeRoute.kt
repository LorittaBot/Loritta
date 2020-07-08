package net.perfectdreams.loritta.website.routes

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.ApplicationCall
import io.ktor.request.path
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.HackyWebSettings
import net.perfectdreams.loritta.website.utils.extensions.respondHtml

class HomeRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/") {
	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		call.respondHtml(
				(loritta as Loritta).newWebsite!!.pageProvider.render(
						"home",
						listOf(
								HackyWebSettings(
										LorittaWebsite.INSTANCE.config.websiteUrl,
										call.request.path().split("/").drop(2).joinToString("/"), // TODO
										loritta.discordInstanceConfig.discord.addBotUrl
								),
								locale
						)
				)
		)
	}
}