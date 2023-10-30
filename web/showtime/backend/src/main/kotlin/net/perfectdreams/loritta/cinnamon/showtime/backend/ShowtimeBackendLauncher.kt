package net.perfectdreams.loritta.cinnamon.showtime.backend

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.etherealgambi.client.EtherealGambiClient
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.EtherealGambiImages
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.config.RootConfig
import net.perfectdreams.loritta.common.locale.LorittaLanguageManager
import net.perfectdreams.loritta.common.utils.config.ConfigUtils
import java.util.*

object ShowtimeBackendLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(ShowtimeBackendLauncher::class, System.getProperty("showtime.config", "showtime.conf"))
        logger.info { "Loaded Showtime Backend's configuration file" }

        val languageManager = LorittaLanguageManager(ShowtimeBackendLauncher::class)

        val services = Pudding.createPostgreSQLPudding(
            rootConfig.pudding.address,
            rootConfig.pudding.database,
            rootConfig.pudding.username,
            rootConfig.pudding.password
        )
        services.setupShutdownHook()

        logger.info { "Started Pudding client!" }

        logger.info { "Loading image informations from EtherealGambi..." }
        val etherealGambiClient = EtherealGambiClient(rootConfig.etherealGambi.apiUrl)
        val etherealGambiImages = EtherealGambiImages(etherealGambiClient)
        etherealGambiImages.loadImagesInfo()


        logger.info { "Loading commands from Loritta..." }
        val http = HttpClient {}
        val commands = Commands(rootConfig.loritta.website, http)
        commands.start()

        val showtime = ShowtimeBackend(
            rootConfig,
            languageManager,
            services,
            etherealGambiClient,
            etherealGambiImages,
            commands
        )
        showtime.start()
    }
}