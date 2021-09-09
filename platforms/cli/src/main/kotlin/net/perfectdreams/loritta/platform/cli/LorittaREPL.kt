package net.perfectdreams.loritta.cinnamon.platform.cli

import net.perfectdreams.loritta.cinnamon.platform.cli.utils.config.RootConfig

class LorittaREPL(val config: RootConfig) {
    val cli = LorittaCLI(config.loritta, config.services.gabrielaImageServer)

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