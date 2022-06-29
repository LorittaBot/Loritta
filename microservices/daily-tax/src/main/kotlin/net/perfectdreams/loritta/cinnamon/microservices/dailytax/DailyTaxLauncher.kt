package net.perfectdreams.loritta.cinnamon.microservices.dailytax

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LorittaLanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import java.util.*
import kotlin.concurrent.thread

object DailyTaxLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(DailyTaxLauncher::class, System.getProperty("dailytax.config", "daily-tax.conf"))
        logger.info { "Loaded Loritta's configuration file" }

        val languageManager = LorittaLanguageManager(DailyTaxLauncher::class)

        val services = Pudding.createPostgreSQLPudding(
            rootConfig.pudding.address,
            rootConfig.pudding.database,
            rootConfig.pudding.username,
            rootConfig.pudding.password
        )

        Runtime.getRuntime().addShutdownHook(
            thread(false) {
                // Shutdown services when stopping the application
                // This is needed for the Pudding Tasks
                services.shutdown()
            }
        )

        logger.info { "Started Pudding client!" }

        val loritta = DailyTax(
            rootConfig,
            services,
            languageManager
        )

        loritta.start()
    }
}