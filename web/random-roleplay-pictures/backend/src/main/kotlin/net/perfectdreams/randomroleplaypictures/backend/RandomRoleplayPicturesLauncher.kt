package net.perfectdreams.randomroleplaypictures.backend

import mu.KotlinLogging
import java.util.*

object RandomRoleplayPicturesLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val showtime = RandomRoleplayPictures()
        showtime.start()
    }
}