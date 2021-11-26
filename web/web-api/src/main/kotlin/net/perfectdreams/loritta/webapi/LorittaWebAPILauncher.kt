package net.perfectdreams.loritta.webapi

object LorittaWebAPILauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val m = LorittaWebAPI()
        m.start()
    }
}