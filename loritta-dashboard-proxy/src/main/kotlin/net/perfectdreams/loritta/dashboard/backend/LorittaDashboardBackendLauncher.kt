package net.perfectdreams.loritta.dashboard.backend

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.harmony.logging.slf4j.HarmonyLoggerCreatorSLF4J
import net.perfectdreams.loritta.dashboard.backend.configs.LorittaDashboardBackendConfig
import java.io.File
import java.util.TreeSet

object LorittaDashboardBackendLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        HarmonyLoggerFactory.setLoggerCreator(HarmonyLoggerCreatorSLF4J())

        // VERY HACKY WORKAROUND to allow using "Host" on the Java client
        // This should be called BEFORE initializing the engine!
        // See https://youtrack.jetbrains.com/issue/KTOR-9158/Cannot-override-the-Host-header-with-the-Java-client-engine
        val clazz = Class.forName("io.ktor.client.engine.java.JavaHttpRequestKt")

        clazz.getDeclaredField("DISALLOWED_HEADERS").apply {
            this.isAccessible = true
            val treeSet = this.get(null) as TreeSet<String>
            treeSet.remove("Host")
        }

        System.setProperty("jdk.httpclient.allowRestrictedHeaders", "host")

        val config = Hocon.decodeFromConfig<LorittaDashboardBackendConfig>(ConfigFactory.parseFile(File("loritta-dashboard.conf")))

        val m = LorittaDashboardBackend(config)
        m.start()
    }
}