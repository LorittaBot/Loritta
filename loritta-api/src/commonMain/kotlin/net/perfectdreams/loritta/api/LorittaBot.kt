package net.perfectdreams.loritta.api

import io.ktor.client.*
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.CommandMap
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
	abstract val commandMap: CommandMap<Command<CommandContext>>
	abstract val pluginManager: PluginManager
	abstract val assets: LorittaAssets
	abstract val http: HttpClient
	abstract val httpWithoutTimeout: HttpClient
	abstract val random: Random
}