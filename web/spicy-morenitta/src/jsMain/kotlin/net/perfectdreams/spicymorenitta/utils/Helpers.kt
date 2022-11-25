package net.perfectdreams.spicymorenitta.utils

// From https://github.com/JetBrains/kotlin-wrappers/blob/5523615ea8f27ace55146e1fa7a19f1948b42212/kotlin-extensions/src/main/kotlin/kotlinext/js/Helpers.kt
@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> jsObject(): T =
    js("({})")

inline fun <T : Any> jsObject(builder: T.() -> Unit): T =
    jsObject<T>().apply(builder)

inline fun js(builder: dynamic.() -> Unit): dynamic = jsObject(builder)