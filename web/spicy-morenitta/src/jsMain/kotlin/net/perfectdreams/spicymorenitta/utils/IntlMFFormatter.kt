package net.perfectdreams.spicymorenitta.utils

import net.perfectdreams.i18nhelper.core.Formatter
import net.perfectdreams.i18nhelper.formatters.IntlMessageFormat

class IntlMFFormatter(val locale: String) : Formatter {
    override fun format(message: String, args: Map<String, Any?>): String {
        // TODO: Cache
        val mf = IntlMessageFormat(message, locale)

        return mf.format(
            jsObject {
                for (arg in args) {
                    this[arg.key] = arg.value
                }
            }
        )
    }

    // From Jetpack Compose Web
    @Suppress("NOTHING_TO_INLINE")
    private inline fun <T : Any> jsObject(): T =
        js("({})")

    private inline fun <T : Any> jsObject(builder: T.() -> Unit): T =
        jsObject<T>().apply(builder)
}