package net.perfectdreams.loritta.platform.amino

import com.fasterxml.jackson.module.kotlin.readValue
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.platform.amino.utils.config.GeneralAminoConfig
import java.io.File

object AminoLorittaLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val configurationFile = File(System.getProperty("conf") ?: "./loritta.conf")
        val config = Constants.HOCON_MAPPER.readValue<GeneralConfig>(configurationFile.readText())

        val aminoConfigurationFile = File(System.getProperty("aminoConf") ?: "./amino.conf")
        val aminoConfig = Constants.HOCON_MAPPER.readValue<GeneralAminoConfig>(aminoConfigurationFile.readText())

        runBlocking {
            val loritta = AminoLoritta(aminoConfig, config)
            loritta.start()
        }
    }
}