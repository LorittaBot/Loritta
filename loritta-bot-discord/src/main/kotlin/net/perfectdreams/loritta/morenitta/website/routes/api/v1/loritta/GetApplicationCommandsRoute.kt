package net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta

import io.ktor.server.application.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclaration
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandGroupDeclaration
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.serializable.ApplicationCommandInfo
import net.perfectdreams.loritta.serializable.SlashCommandGroupInfo
import net.perfectdreams.loritta.serializable.SlashCommandInfo
import net.perfectdreams.sequins.ktor.BaseRoute

class GetApplicationCommandsRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/loritta/application-commands") {
	override suspend fun onRequest(call: ApplicationCall) {
		val unleashedCommands = loritta.interactionsListener.manager.slashCommands
			.map { convertSlashCommandDeclaration(it) }

		call.respondJson(Json.encodeToString<List<ApplicationCommandInfo>>(unleashedCommands))
	}

	private fun convertSlashCommandDeclaration(slashCommandDeclaration: SlashCommandDeclaration): SlashCommandInfo {
		return SlashCommandInfo(
			slashCommandDeclaration.name,
			slashCommandDeclaration.description,
			slashCommandDeclaration.category,
			slashCommandDeclaration.executor?.let { it::class.simpleName },
			slashCommandDeclaration.interactionContexts.singleOrNull() == InteractionContextType.GUILD,
			slashCommandDeclaration.subcommands.map { convertSlashCommandDeclaration(it) },
			slashCommandDeclaration.subcommandGroups.map { convertSlashCommandGroupDeclaration(it) }
		)
	}

	private fun convertSlashCommandGroupDeclaration(slashCommandGroupDeclaration: SlashCommandGroupDeclaration): SlashCommandGroupInfo {
		return SlashCommandGroupInfo(
			slashCommandGroupDeclaration.name,
			slashCommandGroupDeclaration.description,
			slashCommandGroupDeclaration.category,
			slashCommandGroupDeclaration.subcommands.map {
				convertSlashCommandDeclaration(it)
			}
		)
	}
}