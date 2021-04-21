package net.perfectdreams.loritta.platform.interaktions

import com.typesafe.config.ConfigFactory
import mu.KotlinLogging
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.utils.ConfigUtils
import net.perfectdreams.loritta.discord.parseDiscordConfig
import net.perfectdreams.loritta.platform.interaktions.emotes.DiscordEmoteManager
import net.perfectdreams.loritta.platform.interaktions.utils.config.parseDiscordInteractionsConfig
import net.perfectdreams.loritta.platform.interaktions.utils.metrics.Prometheus
import java.io.File

object LorittaInteraKTionsLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val emotes = loadEmotes()
        logger.info { "Loaded ${emotes.size} emotes" }

        Prometheus.register()
        logger.info { "Registered Prometheus Metrics" }

        val loritta = LorittaInteraKTions(
            ConfigUtils.parseConfig(),
            ConfigUtils.parseDiscordConfig(),
            ConfigUtils.parseDiscordInteractionsConfig(),
            Emotes(DiscordEmoteManager(emotes))
        )

        loritta.start()
    }

    private fun loadEmotes(): Map<String, String> {
        val emotesFile = File("./emotes.conf")
        val fileConfig = ConfigFactory.parseFile(emotesFile)

        return fileConfig.entrySet().map {
            it.key to it.value.unwrapped() as String
        }.toMap()
    }
}