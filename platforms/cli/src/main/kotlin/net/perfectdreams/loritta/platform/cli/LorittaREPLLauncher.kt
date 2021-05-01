package net.perfectdreams.loritta.platform.cli

import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.common.utils.ConfigUtils

object LorittaREPLLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val loritta = LorittaREPL(ConfigUtils.parseConfig())
        runBlocking {
            loritta.start()
        }
    }
}