package net.perfectdreams.spicymorenitta.utils

@JsName("moment")
external class Moment(unix: Number? = definedExternally) {
	companion object {
		fun locale(locale: String)
	}
	fun fromNow(): String

	fun add(number: Number, span: String)

	fun format(dyn: dynamic): String
}