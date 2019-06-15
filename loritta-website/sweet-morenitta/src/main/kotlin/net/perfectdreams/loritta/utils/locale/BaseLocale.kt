package net.perfectdreams.loritta.utils.locale

import mu.KotlinLogging
import java.text.MessageFormat

class BaseLocale(val id: String) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val localeEntries = mutableMapOf<String, Any?>()
    val path: String
        get() = this["website.localePath"]

    operator fun get(key: String, vararg arguments: Any?): String {
        try {
            return getWithType<String>(key).f(*arguments)
        } catch (e: RuntimeException) {
            logger.error(e) { "Error when trying to retrieve $key" }
        }
        return "!!{$key}!!"
    }

    fun <T> getWithType(key: String): T {
        val entry = localeEntries[key] ?: throw RuntimeException("Key $key doesn't exist!")
        return entry as T
    }

    fun String.msgFormat(vararg arguments: Any?): String {
        return MessageFormat.format(this, *arguments)
    }

    fun String.f(vararg arguments: Any?): String {
        return msgFormat(*arguments)
    }
}