package net.perfectdreams.loritta.spicymorenitta.dashboard.utils

inline fun jsObject(init: dynamic.() -> Unit): dynamic {
    val o = js("{}")
    init(o)
    return o
}