package net.perfectdreams.showtime.backend

import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager

object ShowtimeBackendLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val languageManager = LanguageManager(
            ShowtimeBackendLauncher::class,
            "en",
            "/languages/"
        )
        languageManager.loadLanguagesAndContexts()

        val showtime = ShowtimeBackend(languageManager)
        showtime.start()
    }
}