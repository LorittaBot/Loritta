package net.perfectdreams.loritta.api

import net.perfectdreams.loritta.api.commands.CommandMap
import net.perfectdreams.loritta.api.platform.PlatformFeature

/**
 * Loritta Morenitta :3
 *
 * This should be extended by plataform specific Lori's
 */
abstract class LorittaBot {
	abstract val supportedFeatures: List<PlatformFeature>
	abstract val commandMap: CommandMap<*>
}