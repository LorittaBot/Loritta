package com.mrpowergamerbr.loritta.utils.extensions

import java.util.*

fun <T> Optional<T>.getOrNull() = if (this.isPresent) this.get() else null