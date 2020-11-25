package net.perfectdreams.loritta.website.routes.api.v1.loritta

import io.ktor.application.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.serializable.CommandInfo
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class GetCommandsRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/loritta/commands/{localeId}") {
	override suspend fun onRequest(call: ApplicationCall) {
		val localeId = call.parameters["localeId"] ?: return

		val locale = loritta.getLocaleById(localeId)
		val legacyLocale = loritta.getLegacyLocaleById(localeId)

		val commands = com.mrpowergamerbr.loritta.utils.loritta.legacyCommandManager.commandMap.map {
			CommandInfo(
					it::class.java.simpleName,
					it.label,
					it.aliases,
					it.category,
					it.getDescription(legacyLocale),
					it.getUsage(legacyLocale).build(locale)
			)
		} + com.mrpowergamerbr.loritta.utils.loritta.commandMap.commands.filter { !it.hideInHelp }.map {
			CommandInfo(
					it.commandName,
					it.labels.first(),
					it.labels.drop(1).toList(),
					it.category,
					it.description.invoke(locale),
					it.usage.build(locale)
			)
		}

		call.respondJson(Json.encodeToString(ListSerializer(CommandInfo.serializer()), commands))
	}
}