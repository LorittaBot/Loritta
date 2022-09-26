package net.perfectdreams.loritta.common.utils

actual fun String.format(vararg arguments: Any?): String {
	var str = this
	arguments.forEachIndexed { index, any ->
		str = str.replace("{$index}", any.toString())
	}
	return str
}