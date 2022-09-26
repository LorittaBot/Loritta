package net.perfectdreams.loritta.common.locale

import kotlin.reflect.KClass

/**
 * Creates a [LanguageManager] with default values and also calls `loadLanguagesAndContexts()` to load all languages
 *
 * @param clazz any class that is in the same classpath as the `/languages/` folder
 * @return a [LanguageManager] instance with all languages and contexts loaded
 */
fun LorittaLanguageManager(clazz: KClass<*>): LanguageManager {
    val languageManager = LanguageManager(
        clazz,
        "en",
        "/languages/"
    )
    languageManager.loadLanguagesAndContexts()
    return languageManager
}