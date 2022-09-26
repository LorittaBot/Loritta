package net.perfectdreams.loritta.morenitta.utils.extensions

import java.util.*

fun <T> Optional<T>.getOrNull() = if (this.isPresent) this.get() else null