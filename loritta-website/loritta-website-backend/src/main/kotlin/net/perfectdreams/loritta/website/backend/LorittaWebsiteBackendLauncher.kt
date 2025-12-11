package net.perfectdreams.loritta.website.backend

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.etherealgambi.client.EtherealGambiClient
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.website.backend.utils.EtherealGambiImages
import net.perfectdreams.loritta.website.backend.utils.config.RootConfig
import net.perfectdreams.loritta.common.locale.LorittaLanguageManager
import net.perfectdreams.loritta.common.utils.config.ConfigUtils
import java.util.*

object LorittaWebsiteBackendLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(LorittaWebsiteBackendLauncher::class, System.getProperty("showtime.config", "showtime.conf"))
        logger.info { "Loaded Showtime Backend's configuration file" }

        val languageManager = LorittaLanguageManager(LorittaWebsiteBackendLauncher::class)

        val services = Pudding.createPostgreSQLPudding(
            122,
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

        val showtime = LorittaWebsiteBackend(
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