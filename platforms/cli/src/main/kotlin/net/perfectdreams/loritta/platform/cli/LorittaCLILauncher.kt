package net.perfectdreams.loritta.platform.cli

import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.common.utils.ConfigUtils

object LorittaCLILauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val loritta = LorittaCLI(ConfigUtils.parseConfig())
        loritta.start()
        runBlocking {
            loritta.runArgs(args)
        }
    }
}