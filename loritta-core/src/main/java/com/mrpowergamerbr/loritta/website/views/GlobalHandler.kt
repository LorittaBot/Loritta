package com.mrpowergamerbr.loritta.website.views

import com.google.common.collect.Lists
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.extensions.valueOrNull
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.website.evaluate
import com.mrpowergamerbr.loritta.website.views.subviews.*
import com.mrpowergamerbr.loritta.website.views.subviews.api.*
import com.mrpowergamerbr.loritta.website.views.subviews.api.config.APIGetApiKeyView
import com.mrpowergamerbr.loritta.website.views.subviews.api.config.APIGetServerConfigView
import com.mrpowergamerbr.loritta.website.views.subviews.api.config.APIUpdateServerConfigView
import com.mrpowergamerbr.loritta.website.views.subviews.configure.*
import org.jooby.Request
import org.jooby.Response
import org.slf4j.LoggerFactory
import java.util.*

object GlobalHandler {
	var views = mutableListOf<AbstractView>()
	var apiViews = mutableListOf<NoVarsView>()

	val logger = LoggerFactory.getLogger(AbstractCommand::class.java)

	@Deprecated(message = "Hacky, hacky, hacky!!!")
	fun render(req: Request, res: Response): String {
		res.header("Lori-Using-Deprecated-Handler", true)
		val queryString = if (req.queryString().isPresent) {
			"?" + req.queryString().get()
		} else {
			""
		}

		if (req.path().matches(Regex("^/dashboard/configure/[0-9]+/testmessage")) || req.path().matches(Regex("^\\/dashboard\\/configure\\/[0-9]+(\\/)(save)"))) {
			val last = loritta.apiCooldown.getOrDefault(req.header("X-Forwarded-For").value(), 0L)

			val diff = System.currentTimeMillis() - last
			if (2500 >= diff) {
				return GSON.toJson(mapOf("api:code" to LoriWebCodes.RATE_LIMITED, "api:message" to "RATE_LIMITED"))
			}

			loritta.apiCooldown[req.header("X-Forwarded-For").value()] = System.currentTimeMillis()
		}

		apiViews.filter { it.handleRender(req, res, req.path()) }
				.forEach { return it.render(req, res, req.path()) }

		// TODO: Deprecated
		val acceptLanguage = req.header("Accept-Language").value("en-US")

		// Vamos parsear!
		val ranges = Lists.reverse<Locale.LanguageRange>(Locale.LanguageRange.parse(acceptLanguage))

		val defaultLocale = LorittaLauncher.loritta.getLegacyLocaleById("default")
		var lorittaLocale = LorittaLauncher.loritta.getLegacyLocaleById("default")
		var locale = LorittaLauncher.loritta.getLocaleById("default")

		var localeId: String? = null

		for (range in ranges) {
			localeId = range.range.toLowerCase()
			var bypassCheck = false
			if (localeId == "pt-br" || localeId == "pt") {
				localeId = "default"
				bypassCheck = true
			}
			if (localeId == "en") {
				localeId = "en-us"
			}
			val parsedLocale = LorittaLauncher.loritta.getLegacyLocaleById(localeId)
			if (bypassCheck || defaultLocale !== parsedLocale) {
				lorittaLocale = parsedLocale
			}
		}

		if (req.param("logout").isSet) {
			req.session().destroy()
		}

		// Para deixar tudo organizadinho (o Google não gosta de locales que usem query strings ou cookies), nós iremos usar subdomínios!
		val languageCode = req.path().split("/").getOrNull(1)

		if (languageCode != null) {
			locale = loritta.locales.values.firstOrNull { it["website.localePath"] == languageCode } ?: locale

			lorittaLocale = when (languageCode) {
				"br" -> LorittaLauncher.loritta.getLegacyLocaleById("default")
				"pt" -> LorittaLauncher.loritta.getLegacyLocaleById("pt-pt")
				"us" -> LorittaLauncher.loritta.getLegacyLocaleById("en-us")
				"es" -> LorittaLauncher.loritta.getLegacyLocaleById("es-es")
				else -> lorittaLocale
			}
		}

		WebsiteUtils.initializeVariables(req, locale, lorittaLocale, languageCode, false)

		var pathNoLanguageCode = req.path()
		val split = pathNoLanguageCode.split("/").toMutableList()
		val languageCode2 = split.getOrNull(1)

		val hasLangCode = loritta.locales.any { it.value["website.localePath"] == languageCode2 }
		if (hasLangCode) {
			split.removeAt(0)
			split.removeAt(0)
			pathNoLanguageCode = "/" + split.joinToString("/")
		} else {
			// Nós iremos redirecionar o usuário para a versão correta para ele, caso esteja acessando o "website errado"
			if (localeId != null) {
				val hostHeader = req.header("Host").valueOrNull()
				if ((req.path() != "/dashboard" && !req.param("discordAuth").isSet) && req.path() != "/auth" && !req.path().matches(Regex("^\\/dashboard\\/configure\\/[0-9]+(\\/)(save)")) && !req.path().matches(Regex("^/dashboard/configure/[0-9]+/testmessage")) && !req.path().startsWith("/translation") /* DEPRECATED API */) {
					res.status(302) // temporary redirect / no page rank penalty (?)
					if (localeId == "default") {
						res.redirect("https://${hostHeader}/br${req.path()}${queryString}")
					}
					if (localeId == "pt-pt") {
						res.redirect("https://${hostHeader}/pt${req.path()}${queryString}")
					}
					if (localeId == "es-es") {
						res.redirect("https://${hostHeader}/es${req.path()}${queryString}")
					}
					res.redirect("https://${hostHeader}/us${req.path()}${queryString}")
					return "Redirecting..."
				}
			}
		}

		val variables = req.get("variables") as MutableMap<String, Any?>
		try {
			views.filter { it.handleRender(req, res, pathNoLanguageCode, variables) }
					.forEach { return it.render(req, res, pathNoLanguageCode, variables) }
		} catch (e: Exception) {
			logger.error("Erro ao processar conteúdo para ${req.header("X-Forwarded-For").value()}: ${req.path()}", e)
			throw e
		}

		res.status(404)
		return evaluate("404.html", variables)
	}

	fun generateViews()  {
		val views = mutableListOf<AbstractView>()
		val apiViews = mutableListOf<NoVarsView>()

		// ===[ APIS ]===
		apiViews.add(APIGetCommunityInfoView())
		apiViews.add(APIGetChannelInfoView())
		apiViews.add(APIGetRssFeedTitleView())
		apiViews.add(APIGetTwitchInfoView())
		apiViews.add(APILoriSetBalanceView())
		apiViews.add(APIGetServerConfigView())
		apiViews.add(APIUpdateServerConfigView())
		apiViews.add(APILoriDailyRewardView())
		apiViews.add(APILoriDailyRewardStatusView())
		apiViews.add(APIGetApiKeyView())
		apiViews.add(APIGetCommandsView())

		views.add(HomeView())
		views.add(TranslationView())
		views.add(DashboardView())
		views.add(CommandsView())
		views.add(DailyView())
		views.add(SupportView())
		views.add(NashornDocsView())
		views.add(TermsOfServiceView())
		views.add(TicTacToeView())
		views.add(AuthPathRedirectView())

		views.add(ConfigureServerView())
		views.add(ConfigureEventLogView())
		views.add(ConfigureInviteBlockerView())
		views.add(ConfigureAutoroleView())
		views.add(ConfigurePermissionsView())
		views.add(ConfigureWelcomerView())
		views.add(ConfigureStarboardView())
		views.add(ConfigureAminoView())
		views.add(ConfigureYouTubeView())
		views.add(ConfigureLivestreamView())
		views.add(ConfigureRSSFeedsView())
		views.add(ConfigureNashornCommandsView())
		views.add(ConfigureMusicView())
		views.add(ConfigureEventHandlersView())
		views.add(ConfigureCommandsView())
		views.add(ConfigureTextChannelsView())
		views.add(ConfigureModerationView())
		views.add(TestMessageView())

		this.views = views
		this.apiViews = apiViews
	}
}