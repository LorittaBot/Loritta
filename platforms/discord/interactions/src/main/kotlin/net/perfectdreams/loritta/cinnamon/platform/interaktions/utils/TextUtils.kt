package net.perfectdreams.loritta.cinnamon.platform.interaktions.utils

fun String.shortenWithEllipsis(): String {
    if (this.length >= 100)
        return this.take(97) + "..."
    return this
}