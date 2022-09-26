package net.perfectdreams.loritta.legacy.api.utils

actual fun String.format(vararg arguments: Any?): String {
	var str = this
	arguments.forEachIndexed { index, any ->
		str = str.replace("{$index}", any.toString())
	}
	return str
}