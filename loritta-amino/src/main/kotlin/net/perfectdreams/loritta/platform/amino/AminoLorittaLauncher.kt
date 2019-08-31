package net.perfectdreams.loritta.platform.amino

import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralInstanceConfig
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.platform.amino.utils.config.GeneralAminoConfig
import net.perfectdreams.loritta.utils.readConfigurationFromFile
import java.io.File

object AminoLorittaLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = readConfigurationFromFile<GeneralConfig>(File(System.getProperty("conf") ?: "./loritta.conf"))
        val instanceConfig = readConfigurationFromFile<GeneralInstanceConfig>(File(System.getProperty("conf") ?: "./loritta.instance.conf"))
        val aminoConfig = readConfigurationFromFile<GeneralAminoConfig>(File(System.getProperty("aminoConf") ?: "./amino.conf"))

        runBlocking {
            val loritta = AminoLoritta(aminoConfig, config, instanceConfig)
            loritta.start()
        }
    }
}