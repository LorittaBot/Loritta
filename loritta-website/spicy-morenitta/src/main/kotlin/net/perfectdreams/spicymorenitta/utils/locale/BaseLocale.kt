package net.perfectdreams.spicymorenitta.utils.locale

import kotlinx.serialization.Serializable
import net.perfectdreams.spicymorenitta.utils.Logging

@Serializable
class BaseLocale(
        val id: String,
        val localeEntries: Map<String, String>
) : Logging {
    val path: String
        get() = this["website.localePath"]

    operator fun get(key: String, vararg arguments: Any?): String {
        try {
            return getWithType<String>(key).f(*arguments)
        } catch (e: RuntimeException) {
            error("Error when trying to retrieve $key")
        }
        return "!!{$key}!!"
    }

    fun <T> getWithType(key: String): T {
        val entry = localeEntries[key] ?: throw RuntimeException("Key $key doesn't exist!")
        return entry as T
    }

    fun String.msgFormat(vararg arguments: Any?): String {
        var str = this
        arguments.forEachIndexed { index, any ->
            str = str.replace("{$index}", any.toString())
        }
        return str
    }

    fun String.f(vararg arguments: Any?): String {
        return msgFormat(*arguments)
    }
}