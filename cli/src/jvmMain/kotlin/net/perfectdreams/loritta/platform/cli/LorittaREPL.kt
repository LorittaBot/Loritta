package net.perfectdreams.loritta.platform.cli

class LorittaREPL {
    val cli = LorittaCLI()

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