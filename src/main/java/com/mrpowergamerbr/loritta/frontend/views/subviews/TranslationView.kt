package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.mrpowergamerbr.loritta.Loritta.Companion.gson
import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.utils.loritta
import org.jooby.Request
import org.jooby.Response

class TranslationView : AbstractView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any>): Boolean {
		return req.path().startsWith("/translation")
	}

	override fun render(req: Request, res: Response, variables: MutableMap<String, Any>): String {
		val split = req.path().split("/")
		var localeId = "default"
		val defaultLocale = loritta.getLocaleById("default")
		val locale = if (split.size == 3) {
			localeId = split[2]
			loritta.getLocaleById(split[2])
		} else {
			loritta.getLocaleById("default")
		}
		val localeStrings = mutableMapOf<String, String>()
		for ((key, value) in locale.strings) {
			localeStrings[key.replace("[Translate!]", "")] = value
		}
		variables["locale_strings"] = localeStrings
		variables["original_strings"] = defaultLocale.strings
		variables["original_strings_as_json"] = gson.toJson(defaultLocale.strings)
		variables["localeId"] = localeId
		return evaluate("translate_tool.html", variables)
	}

}