package net.perfectdreams.showtime.backend

object ShowtimeBackendLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val showtime = ShowtimeBackend()
        showtime.start()
    }
}