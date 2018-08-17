package com.mrpowergamerbr.loritta.utils.extensions

import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM

fun <T> List<T>.getRandom(): T {
	return this[RANDOM.nextInt(this.size)]
}