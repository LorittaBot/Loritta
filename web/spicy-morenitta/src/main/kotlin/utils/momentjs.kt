package utils

@JsName("moment")
external class Moment(unix: Number) {
	companion object {
		fun locale(locale: String)
		fun unix(unix: Number): Moment
	}

	fun fromNow(): String
	fun calendar(): String

	fun add(number: Number, span: String)
}