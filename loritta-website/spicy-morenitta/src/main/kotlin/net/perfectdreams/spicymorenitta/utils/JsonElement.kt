package net.perfectdreams.spicymorenitta.utils

open class JsonElement(val backed: Any?) {
	val long: Long = backed.toString().toLong()

	val string: String
		get() = backed.toString()
}