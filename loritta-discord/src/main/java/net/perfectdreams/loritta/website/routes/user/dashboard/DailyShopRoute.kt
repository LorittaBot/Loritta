package net.perfectdreams.loritta.website.routes.user.dashboard

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.ApplicationCall
import io.ktor.request.header
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class DailyShopRoute(loritta: LorittaDiscord) : RequiresDiscordLoginLocalizedRoute(loritta, "/user/@me/dashboard/daily-shop") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val variables = call.legacyVariables(locale)

		variables["saveType"] = "daily_shop"

		call.respondHtml(evaluate("profile_dashboard_daily_shop.html", variables))
	}

	override suspend fun onUnauthenticatedRequest(call: ApplicationCall, locale: BaseLocale) {
		if (call.request.header("User-Agent") == Constants.DISCORD_CRAWLER_USER_AGENT) {
			call.respondHtml(
					createHTML().html {
						head {
							fun setMetaProperty(property: String, content: String) {
								meta(content = content) { attributes["property"] = property }
							}
							title("Login • Loritta")
							setMetaProperty("og:site_name", "Loritta")
							setMetaProperty("og:title", "Loja Diária")
							setMetaProperty("og:description", "Bem-vind@ a loja diária de itens! O lugar para comprar itens para o seu \"+perfil\" da Loritta!\n\nTodo o dia as 00:00 UTC (21:00 no horário do Brasil) a loja é atualizada com novos itens! Então volte todo o dia para verificar ^-^")
							setMetaProperty("og:image", com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.website.url + "assets/img/loritta_daily_shop.png")
							setMetaProperty("og:image:width", "320")
							setMetaProperty("og:ttl", "660")
							setMetaProperty("og:image:width", "320")
							setMetaProperty("theme-color", "#7289da")
							meta("twitter:card", "summary_large_image")
						}
						body {
							p {
								+"Parabéns, você encontrou um easter egg!"
							}
						}
					}
			)
		}
		return super.onUnauthenticatedRequest(call, locale)
	}
}