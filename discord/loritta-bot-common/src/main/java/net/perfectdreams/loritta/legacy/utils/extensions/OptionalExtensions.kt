package net.perfectdreams.loritta.legacy.utils.extensions

import java.util.*

fun <T> Optional<T>.getOrNull() = if (this.isPresent) this.get() else null