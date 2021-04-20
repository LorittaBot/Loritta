package net.perfectdreams.loritta.platform.cli

import net.perfectdreams.loritta.common.utils.config.LorittaConfig

class LorittaREPL(val config: LorittaConfig) {
    val cli = LorittaCLI(config)

    suspend fun start() {
        println("Loritta Morenitta REPL")

        cli.start()

        while (true) {
            try {
                val line = readLine() ?: continue
                cli.runArgs(line.split(" ").toTypedArray())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}