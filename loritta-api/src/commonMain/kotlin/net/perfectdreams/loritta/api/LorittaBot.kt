package net.perfectdreams.loritta.api

import io.ktor.client.*
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.CommandManager
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.platform.PlatformFeature
import net.perfectdreams.loritta.api.plugin.PluginManager
import net.perfectdreams.loritta.api.utils.LorittaAssets
import kotlin.random.Random

/**
 * Loritta Morenitta :3
 *
 * This should be extended by plataform specific Lori's
 */
abstract class LorittaBot {
	abstract val supportedFeatures: List<PlatformFeature>
	abstract val commandManager: CommandManager<LorittaCommand<CommandContext>>
	abstract val pluginManager: PluginManager
	abstract val assets: LorittaAssets
	abstract val http: HttpClient
	abstract val httpWithoutTimeout: HttpClient
	abstract val random: Random
}