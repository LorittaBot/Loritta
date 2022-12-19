package net.perfectdreams.loritta.lorituber.server

import net.perfectdreams.loritta.cinnamon.pudding.Pudding

object LoriTuberServerLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val m = LoriTuberServer(
            Pudding.createPostgreSQLPudding(
                "127.0.0.1",
                "cinnamon",
                "postgres",
                "postgres",
            )
        )
        m.start()
    }
}