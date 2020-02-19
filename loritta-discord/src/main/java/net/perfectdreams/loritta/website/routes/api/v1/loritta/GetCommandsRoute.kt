package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class GetCommandsRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/loritta/commands/{localeId}") {
	override suspend fun onRequest(call: ApplicationCall) {
		val localeId = call.parameters["localeId"] ?: return

		val locale = loritta.getLocaleById(localeId)
		val legacyLocale = loritta.getLegacyLocaleById(localeId)
		val array = JsonArray()

		com.mrpowergamerbr.loritta.utils.loritta.legacyCommandManager.commandMap.forEach {
			val obj = JsonObject()
			obj["name"] = it::class.java.simpleName
			obj["label"] = it.label
			obj["aliases"] = it.aliases.toJsonArray()
			obj["category"] = it.category.name
			obj["description"] = it.getDescription(legacyLocale)
			obj["usage"] = it.getUsage()
			obj["detailedUsage"] = Loritta.GSON.toJsonTree(it.getDetailedUsage())
			// obj["example"] = it.getExamples().toJsonArray()
			obj["extendedExamples"] = Loritta.GSON.toJsonTree(it.getExtendedExamples())
			obj["requiredUserPermissions"] = it.getDiscordPermissions().map { it.name }.toJsonArray()
			obj["requiredBotPermissions"] = it.getBotPermissions().map { it.name }.toJsonArray()
			array.add(obj)
		}

		com.mrpowergamerbr.loritta.utils.loritta.commandManager.commands.forEach {
			val obj = JsonObject()
			obj["name"] = it::class.java.simpleName
			obj["label"] = it.labels.first()
			obj["aliases"] = it.labels.toList().toJsonArray()
			obj["category"] = it.category.name
			obj["description"] = it.getDescription(locale)
			obj["usage"] = it.getUsage(locale).build(locale)
			obj["detailedUsage"] = jsonObject()
			// obj["example"] = it.getExamples(locale).toJsonArray()
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

		com.mrpowergamerbr.loritta.utils.loritta.commandMap.commands.forEach {
			val obj = JsonObject()
			obj["name"] = it::class.java.simpleName
			obj["label"] = it.labels.first()
			obj["aliases"] = it.labels.toList().toJsonArray()
			obj["category"] = it.category.name
			obj["description"] = it.description.invoke(locale)
			obj["usage"] = it.usage.build(locale)
			obj["detailedUsage"] = jsonObject()
			// obj["example"] = it.getExamples(locale).toJsonArray()
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

		call.respondJson(array)
	}
}