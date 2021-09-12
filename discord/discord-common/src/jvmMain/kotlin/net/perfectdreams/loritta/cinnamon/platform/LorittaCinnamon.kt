package net.perfectdreams.loritta.cinnamon.platform

import dev.kord.rest.service.RestClient
import io.ktor.client.*
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.config.DiscordInteractionsConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.config.LorittaDiscordConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.config.ServicesConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import kotlin.random.Random

/**
 * Represents a Loritta Morenitta (Cinnamon) implementation.
 *
 * This should be extended by other modules :3
 */
abstract class LorittaCinnamon(
    val config: LorittaConfig,
    val discordConfig: LorittaDiscordConfig,
    val interactionsConfig: DiscordInteractionsConfig,
    val servicesConfig: ServicesConfig,

    val languageManager: LanguageManager,
    val services: Pudding,
    val http: HttpClient
) {
    // TODO: *Really* set a random seed
    val random = Random(0)

    val rest = RestClient(discordConfig.token)
}