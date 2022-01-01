package net.perfectdreams.loritta.webapi

import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager

object LorittaWebAPILauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val languageManager = LanguageManager(
            LorittaWebAPI::class,
            "en",
            "/languages/"
        )
        languageManager.loadLanguagesAndContexts()

        val m = LorittaWebAPI(languageManager)
        m.start()
    }
}