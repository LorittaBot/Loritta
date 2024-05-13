package net.perfectdreams.loritta.morenitta.website.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AuthorizeScopeURL
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.hostFromHeader
import net.perfectdreams.loritta.morenitta.website.utils.extensions.redirect
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.serializable.EmbeddedSpicyModal
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

abstract class LocalizedRoute(val loritta: LorittaBot, val originalPath: String) : BaseRoute("/{localeId}$originalPath") {
	open val isMainClusterOnlyRoute = false

	override suspend fun onRequest(call: ApplicationCall) {
		if (isMainClusterOnlyRoute && !loritta.isMainInstance)
		// If this is a main cluster only route, we are going to redirect to Loritta's main website
			redirect(loritta.config.loritta.website.url.removeSuffix("/") + call.request.path(), true)

		val localeIdFromPath = call.parameters["localeId"]

		// Pegar a locale da URL e, caso não existir, faça fallback para o padrão BR
		val locale = loritta.localeManager.locales.values.firstOrNull { it.path == localeIdFromPath }
		if (locale != null) {
			val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(locale.id)

			try {
				return onLocalizedRequest(
					call,
					locale,
					i18nContext
				)
			} catch (e: Exception) {
				if (call.request.header("HX-Request") != null && (e is TemmieDiscordAuth.TokenExchangeException || e is TemmieDiscordAuth.TokenUnauthorizedException)) {
					// If this is a HX-Request and we caught an exception, let's show a fancy modal to the user asking them to reauthorize!
					val hostHeader = call.request.hostFromHeader()
					val scheme = LorittaWebsite.WEBSITE_URL.split(":").first()
					val redirectUrl = LorittaDiscordOAuth2AuthorizeScopeURL(loritta, "$scheme://$hostHeader" + call.request.path()).toString()

					// This works, but it looks like it sometimes doesn't work because of parallel requests (so the session is cleared but another response causes it to be reset)
					// It doesn't really matter that much because the user will click the "Reauthorize" button and will complete the OAuth2 flow anyway
					call.sessions.clear<LorittaJsonWebSession>()
					call.response.header("SpicyMorenitta-Use-Response-As-HXTrigger", "true")
					call.respondJson(
						buildJsonObject {
							put("playSoundEffect", "config-error")
							put(
								"showSpicyModal",
								EmbeddedSpicyModalUtils.encodeURIComponent(
									Json.encodeToString(
										EmbeddedSpicyModal(
											i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedModal.Title),
											false,
											createHTML()
												.div {
													style = "text-align: center;"

													img(src = "https://stuff.loritta.website/emotes/lori-sob.png") {
														height = "200"
													}

													for (line in i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedModal.Description)) {
														p {
															text(line)
														}
													}
												},
											listOf(
												createHTML()
													.a(href = redirectUrl) {
														button(classes = "discord-button primary") {
															text(i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedModal.Reauthorize))
														}
													}
											)
										)
									)
								)
							)
						}.toString(),
						status = HttpStatusCode.Forbidden
					)
					return
				}
				throw e
			}
		}
	}

	abstract suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext)

	fun getPathWithoutLocale(call: ApplicationCall) = call.request.path().split("/").drop(2).joinToString("/")
}