package utils

@JsName("moment")
external class Moment(unix: Long) {
	companion object {
		fun locale(locale: String)
	}
	fun fromNow(): String

	fun add(number: Number, span: String)
}