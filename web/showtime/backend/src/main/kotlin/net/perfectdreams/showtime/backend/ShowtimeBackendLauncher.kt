package net.perfectdreams.showtime.backend

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.showtime.backend.utils.config.RootConfig
import java.util.*

object ShowtimeBackendLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(ShowtimeBackendLauncher::class, System.getProperty("showtime.config", "showtime.conf"))
        logger.info { "Loaded Showtime Backend's configuration file" }

        val languageManager = LanguageManager(
            ShowtimeBackendLauncher::class,
            "en",
            "/languages/"
        )
        languageManager.loadLanguagesAndContexts()

        val showtime = ShowtimeBackend(rootConfig, languageManager)
        showtime.start()
    }
}