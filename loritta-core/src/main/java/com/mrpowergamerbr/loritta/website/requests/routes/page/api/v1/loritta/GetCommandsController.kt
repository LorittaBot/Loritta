package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/loritta/commands/:localeId")
class GetCommandsController {
	@GET
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response, localeId: String) {
		res.type(MediaType.json)

		val locale = loritta.getLocaleById(localeId)
		val legacyLocale = loritta.getLegacyLocaleById(localeId)
		val array = JsonArray()

		loritta.legacyCommandManager.commandMap.forEach {
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

		loritta.commandManager.commands.forEach {
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

		res.send(
				gson.toJson(array)
		)
	}
}