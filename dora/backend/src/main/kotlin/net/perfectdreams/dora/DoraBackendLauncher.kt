package net.perfectdreams.dora

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.harmony.logging.slf4j.HarmonyLoggerCreatorSLF4J
import net.perfectdreams.pudding.Pudding
import java.io.File

object DoraBackendLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        HarmonyLoggerFactory.setLoggerCreator(HarmonyLoggerCreatorSLF4J())

        val config = Hocon.decodeFromConfig<DoraConfig>(ConfigFactory.parseFile(File("dora.conf")))

        val pudding = Pudding.createPostgreSQL(
            1,
            "Dora",
            config.database.address,
            config.database.database,
            config.database.username,
            config.database.password,
        )

        val m = DoraBackend(config, pudding)
        m.start()
    }
}