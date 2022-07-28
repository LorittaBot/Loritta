package net.perfectdreams.loritta.cinnamon.microservices.correiospackagetracker

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.microservices.correiospackagetracker.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import java.util.*

object CorreiosPackageTrackerLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(CorreiosPackageTrackerLauncher::class, System.getProperty("correiospackagetracker.config", "correios-package-tracker.conf"))
        logger.info { "Loaded Loritta's configuration file" }

        val services = Pudding.createPostgreSQLPudding(
            rootConfig.pudding.address,
            rootConfig.pudding.database,
            rootConfig.pudding.username,
            rootConfig.pudding.password
        )
        services.setupShutdownHook()

        logger.info { "Started Pudding client!" }

        val loritta = CorreiosPackageTracker(
            rootConfig,
            services
        )

        loritta.start()
    }
}