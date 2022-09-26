package net.perfectdreams.loritta.legacy.api

import io.ktor.client.*
import net.perfectdreams.loritta.legacy.api.commands.Command
import net.perfectdreams.loritta.legacy.api.commands.CommandContext
import net.perfectdreams.loritta.legacy.api.commands.CommandMap
import net.perfectdreams.loritta.legacy.api.utils.LorittaAssets

/**
 * Loritta Morenitta :3
 *
 * This should be extended by plataform specific Lori's
 */
abstract class LorittaBot : net.perfectdreams.loritta.legacy.common.LorittaBot() {
	abstract val commandMap: CommandMap<Command<CommandContext>>
	abstract val assets: LorittaAssets
	abstract val http: HttpClient
	abstract val httpWithoutTimeout: HttpClient
}