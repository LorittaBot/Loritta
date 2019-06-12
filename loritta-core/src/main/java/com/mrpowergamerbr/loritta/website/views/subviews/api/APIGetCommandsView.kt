package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.toJsonArray
import com.google.common.collect.Lists
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import java.util.*

class APIGetCommandsView : NoVarsView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/misc/get-commands"))
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)
		val acceptLanguage = req.header("Accept-Language").value("en-US")

		// Vamos parsear!
		val ranges = Lists.reverse<Locale.LanguageRange>(Locale.LanguageRange.parse(acceptLanguage))

		val defaultLocale = LorittaLauncher.loritta.getLegacyLocaleById("default")
		var lorittaLocale = LorittaLauncher.loritta.getLegacyLocaleById("default")

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
			val parsedLocale = LorittaLauncher.loritta.getLegacyLocaleById(localeId)
			if (bypassCheck || defaultLocale !== parsedLocale) {
				lorittaLocale = parsedLocale
			}
		}

		if (req.param("force_locale").isSet) {
			req.session()["forceLocale"] = req.param("force_locale").value()
		}

		if (req.session().isSet("forceLocale")) {
			lorittaLocale  = LorittaLauncher.loritta.getLegacyLocaleById(req.session()["forceLocale"].value())
		}

		if (req.param("locale").isSet) {
			lorittaLocale = LorittaLauncher.loritta.getLegacyLocaleById(req.param("locale").value())
		}

		val array = JsonArray()

		loritta.legacyCommandManager.commandMap.forEach {
			val obj = JsonObject()
			obj["name"] = it::class.java.simpleName
			obj["label"] = it.label
			obj["aliases"] = it.aliases.toJsonArray()
			obj["category"] = it.category.name
			obj["description"] = it.getDescription(lorittaLocale)
			obj["usage"] = it.getUsage()
			obj["detailedUsage"] = GSON.toJsonTree(it.getDetailedUsage())
			obj["example"] = it.getExamples().toJsonArray()
			obj["extendedExamples"] = GSON.toJsonTree(it.getExtendedExamples())
			obj["requiredUserPermissions"] = it.getDiscordPermissions().map { it.name }.toJsonArray()
			obj["requiredBotPermissions"] = it.getBotPermissions().map { it.name }.toJsonArray()
			array.add(obj)
		}

		loritta.commandManager.commands.forEach {
			val obj = JsonObject()
			obj["name"] = it::class.java.simpleName
			obj["label"] = it.labels.first()
			obj["aliases"] = it.labels.toList().toJsonArray()
			obj["category"] = it.category.name
			obj["description"] = it.getDescription(loritta.getLocaleById("default"))
			obj["usage"] = it.getUsage(loritta.getLocaleById("default")).build(loritta.getLocaleById("default"))
			obj["detailedUsage"] = jsonObject()
			obj["example"] = it.getExamples(loritta.getLocaleById("default")).toJsonArray()
			obj["extendedExamples"] = jsonObject()
			if (it is LorittaDiscordCommand) {
				obj["requiredUserPermissions"] = it.discordPermissions.map { it.name }.toJsonArray()
				obj["requiredBotPermissions"] = it.botPermissions.map { it.name }.toJsonArray()
			} else {
				obj["requiredUserPermissions"] = JsonArray()
				obj["requiredBotPermissions"] = JsonArray()
			}
			array.add(obj)
		}

		return gson.toJson(array)
	}
}