package net.perfectdreams.loritta.cinnamon

import dev.kord.rest.service.RestClient
import io.ktor.client.*
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.services.Services
import net.perfectdreams.loritta.cinnamon.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.discord.LorittaDiscordConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.config.DiscordInteractionsConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.config.ServicesConfig
import kotlin.random.Random

abstract class LorittaCinnamon(
    val config: LorittaConfig,
    val discordConfig: LorittaDiscordConfig,
    val interactionsConfig: DiscordInteractionsConfig,
    val servicesConfig: ServicesConfig,

    val languageManager: LanguageManager,
    val services: Services,
    val emotes: Emotes,
    val http: HttpClient
) {

    // TODO: *Really* set a random seed
    val random = Random(0)

    val rest = RestClient(discordConfig.token)
}