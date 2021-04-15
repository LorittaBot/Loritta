package net.perfectdreams.loritta.platform.cli

suspend fun main(args: Array<String>) {
    val loritta = LorittaREPL()
    loritta.start()
}