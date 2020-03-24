package net.perfectdreams.loritta.plugin.lorittabirthday2020.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.request.path
import io.ktor.response.respondText
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import java.io.File

class ViewCurrentStatsRoute(loritta: LorittaDiscord) : RequiresDiscordLoginLocalizedRoute(loritta, "/birthday-2020/stats") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val html = ScriptingUtils.evaluateWebPageFromTemplate(
				File(
						"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/birthday_2020_stats.kts"
				),
				mapOf(
						"path" to call.request.path().split("/").drop(2).joinToString("/"),
						"websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
						"locale" to locale
				)
		)

		call.respondText(html, ContentType.Text.Html)
	}
}