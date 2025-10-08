package net.perfectdreams.loritta.dashboard.backend

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.harmony.logging.slf4j.HarmonyLoggerCreatorSLF4J
import net.perfectdreams.loritta.dashboard.backend.configs.LorittaDashboardBackendConfig
import java.io.File

object LorittaDashboardBackendLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        HarmonyLoggerFactory.setLoggerCreator(HarmonyLoggerCreatorSLF4J())

        val config = Hocon.decodeFromConfig<LorittaDashboardBackendConfig>(ConfigFactory.parseFile(File("loritta-dashboard.conf")))

        val m = LorittaDashboardBackend(config)
        m.start()
    }
}