package net.perfectdreams.loritta.dashboard.backend

object LorittaDashboardBackendLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val m = LorittaDashboardBackend()
        m.start()
    }
}