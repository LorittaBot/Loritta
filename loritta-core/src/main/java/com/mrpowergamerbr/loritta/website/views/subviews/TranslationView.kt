package com.mrpowergamerbr.loritta.website.views.subviews

import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.GsonBuilder
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.evaluate
import org.jooby.Request
import org.jooby.Response
import java.io.File

class TranslationView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path.startsWith("/translation")
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		val split = path.split("/")
		var localeId = "default"
		val defaultLocale = loritta.getLegacyLocaleById("default")
		var locale = if (split.size >= 3) {
			localeId = split[2]
			loritta.getLegacyLocaleById(split[2])
		} else {
			loritta.getLegacyLocaleById("default")
		}
		if (req.ifFile("uploaded-json").isPresent) {
			val upload = req.file("uploaded-json")
			val file = upload.file()
			val json = jsonParser.parse(file.readText()).obj
			locale = LegacyBaseLocale()
			json.entrySet()
					.filter { it.value.isJsonPrimitive && it.value.asJsonPrimitive.isString }
					.forEach { locale.strings[it.key.replace("[Translate!]", "")] = it.value.string }
			localeId = upload.name().replace(".json", "")
			upload.close()
		}
		if (split.size == 4 && split[3] == "save") {
			if (variables.containsKey("discordAuth")) {
				val temmie = variables["discordAuth"] as TemmieDiscordAuth
				if (loritta.config.isOwner(temmie.getUserIdentification().id)) {
					val receivedLocale = jsonParser.parse(req.body().value()).obj
					var inputFile = File(Loritta.LOCALES, "$localeId.json")
					if (!inputFile.exists()) {
						inputFile = File(Loritta.LOCALES, "default.json")
					}

					val originalLocale = jsonParser.parse(inputFile.readText()).obj

					receivedLocale.entrySet()
							.filter { it.value.asJsonPrimitive.isString }
							.forEach { originalLocale[it.key] = it.value.string }

					val outputFile = File(Loritta.LOCALES, "$localeId.json")
					val prettyGson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
					outputFile.writeText(prettyGson.toJson(originalLocale))

					loritta.loadLegacyLocales()
					return "$localeId foi salvo com sucesso!"
				}
			}
			return "\uD83E\uDD37"
		}
		val localeStrings = mutableMapOf<String, String>()
		for ((key, value) in locale.strings) {
			localeStrings[key.replace("[Translate!]", "")] = value
		}
		variables["locale_strings"] = localeStrings
		variables["original_strings"] = defaultLocale.strings
		variables["original_strings_as_json"] = GSON.toJson(defaultLocale.strings)
		variables["localeId"] = localeId
		return evaluate("translate_tool.html", variables)
	}

}