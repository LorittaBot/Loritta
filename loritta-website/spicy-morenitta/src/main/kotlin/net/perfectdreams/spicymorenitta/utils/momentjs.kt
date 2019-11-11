package net.perfectdreams.spicymorenitta.utils

@JsName("moment")
external class Moment(unix: Number) {
	companion object {
		fun locale(locale: String)
	}
	fun fromNow(): String

	fun add(number: Number, span: String)
}