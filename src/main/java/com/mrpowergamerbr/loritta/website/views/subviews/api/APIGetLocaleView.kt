package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.google.common.collect.Lists
import com.google.gson.Gson
import com.mrpowergamerbr.loritta.LorittaLauncher
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import java.util.*

@Deprecated(message = "Now embed within the page")
class APIGetLocaleView : NoVarsView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/misc/get-locale"))
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)
		val acceptLanguage = req.header("Accept-Language").value("en-US")

		// Vamos parsear!
		val ranges = Lists.reverse<Locale.LanguageRange>(Locale.LanguageRange.parse(acceptLanguage))

		val defaultLocale = LorittaLauncher.loritta.getLocaleById("default")
		var lorittaLocale = LorittaLauncher.loritta.getLocaleById("default")

		for (range in ranges) {
			var localeId = range.range.toLowerCase()
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

		if (req.param("force_locale").isSet) {
			req.session()["forceLocale"] = req.param("force_locale").value()
		}

		if (req.session().isSet("forceLocale")) {
			lorittaLocale  = LorittaLauncher.loritta.getLocaleById(req.session()["forceLocale"].value())
		}

		if (req.param("locale").isSet) {
			lorittaLocale = LorittaLauncher.loritta.getLocaleById(req.param("locale").value())
		}
		return Gson().toJson(lorittaLocale.strings)
	}
}