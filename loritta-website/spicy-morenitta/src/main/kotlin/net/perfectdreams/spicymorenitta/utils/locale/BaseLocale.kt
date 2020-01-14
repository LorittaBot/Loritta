package net.perfectdreams.spicymorenitta.utils.locale

import kotlinx.serialization.Serializable
import net.perfectdreams.spicymorenitta.utils.Logging

@Serializable
class BaseLocale(
        val id: String,
        val localeEntries: MutableMap<String, String>
) : Logging {
    val path: String
        get() = this["website.localePath"]

    operator fun get(key: String, vararg arguments: Any?): String {
        try {
            val result = getWithType<String>(key).f(*arguments)

            // Para funcionar igual em Java
            val builder = StringBuilder()

            var isQuotes = false

            for (ch in result) {
                if (ch == '\'') {
                    if (isQuotes) {
                        builder.append('\'')
                        isQuotes = false
                        continue
                    }
                    isQuotes = true
                    continue
                }

                isQuotes = false
                builder.append(ch)
            }

            return builder.toString().f(*arguments)
        } catch (e: RuntimeException) {
            error("Error when trying to retrieve $key")
        }
        return "!!{$key}!!"
    }

    fun <T> getWithType(key: String): T {
        val entry = localeEntries[key] ?: throw RuntimeException("Key $key doesn't exist!")
        return entry as T
    }

    fun getList(key: String): List<String> {
        val entry = getWithType<String>(key)

        if (entry.startsWith("list::")) {
            val list = mutableListOf<String>()
            list.addAll(entry.replaceFirst("list::", "").split("\n"))
            return list
        } else {
            error("Error when trying to retrieve list with $key, missing \"list::\"!")
        }
        throw RuntimeException()
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

    fun buildAsHtml(updateString: String, onControlChar: (Int) -> (Unit), onStringBuild: (String) -> (Unit)) {
        var isControl = false
        var ignoreNext = false

        val genericStringBuilder = StringBuilder()

        for (ch in updateString) {
            if (ignoreNext) {
                ignoreNext = false
                continue
            }
            if (isControl) {
                ignoreNext = true
                isControl = false

                val num = ch.toString().toInt()

                if (genericStringBuilder.isNotEmpty()) {
                    onStringBuild.invoke(genericStringBuilder.toString())
                    genericStringBuilder.clear()
                }

                onControlChar.invoke(num)
                continue
            }
            if (ch == '{') {
                isControl = true
                continue
            }

            genericStringBuilder.append(ch)
        }

        onStringBuild.invoke(genericStringBuilder.toString())
    }
}