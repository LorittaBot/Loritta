package com.mrpowergamerbr.loritta.frontend.views

import com.github.salomonbrys.kotson.fromJson
import com.google.common.collect.Lists
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.frontend.views.subviews.AbstractView
import com.mrpowergamerbr.loritta.frontend.views.subviews.DashboardView
import com.mrpowergamerbr.loritta.frontend.views.subviews.HomeView
import com.mrpowergamerbr.loritta.frontend.views.subviews.TranslationView
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import org.jooby.Request
import org.jooby.Response
import java.util.*

object GlobalHandler {
	fun render(req: Request, res: Response): String {
		val views = getViews()

		val variables = mutableMapOf<String, Any?>("discordAuth" to null)

		val acceptLanguage = req.header("Accept-Language").value("en-US")

		// Vamos parsear!
		val ranges = Lists.reverse<Locale.LanguageRange>(Locale.LanguageRange.parse(acceptLanguage))

		val defaultLocale = LorittaLauncher.loritta.getLocaleById("default")
		var lorittaLocale = LorittaLauncher.loritta.getLocaleById("default")

		for (range in ranges) {
			var localeId = range.getRange().toLowerCase()
			var bypassCheck = false
			if (localeId == "pt-br" || localeId == "pt") {
				localeId = "default"
				bypassCheck = true
			}
			if (localeId == "en") {
				localeId = "en-us"
			}
			val parsedLocale = LorittaLauncher.loritta.getLocaleById(localeId)
			if (bypassCheck || defaultLocale !== parsedLocale) {
				lorittaLocale = parsedLocale
			}
		}

		if (req.param("locale").isSet) {
			lorittaLocale = LorittaLauncher.loritta.getLocaleById(req.param("locale").value())
		}

		for (locale in lorittaLocale.strings) {
			variables[locale.key] = locale.value
		}

		if (req.session().isSet("discordAuth")) {
			val discordAuth = Loritta.gson.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
			try {
				discordAuth.isReady(true)
				variables["discordAuth"] = discordAuth
			} catch (e: Exception) {
				req.session().unset("discordAuth")
			}
		}

		views.filter { it.handleRender(req, res, variables) }
			.forEach { return it.render(req, res, variables) }

		return "404"
	}

	private fun getViews(): List<AbstractView> {
		val views = mutableListOf<AbstractView>()
		views.add(HomeView())
		views.add(TranslationView())
		views.add(DashboardView())
		return views
	}
}