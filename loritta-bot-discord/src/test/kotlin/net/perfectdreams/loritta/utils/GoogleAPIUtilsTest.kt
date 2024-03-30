package net.perfectdreams.loritta.utils

import net.perfectdreams.loritta.cinnamon.discord.utils.google.GoogleAPIUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.google.GoogleVisionLanguage
import org.junit.jupiter.api.Test

class GoogleAPIUtilsTest {
    @Test
    fun `vision language to translate language conversion`() {
        for (visionLanguage in GoogleVisionLanguage.values()) {
            val language = GoogleAPIUtils.fromVisionLanguageToTranslateLanguage(visionLanguage)
        }
    }
}