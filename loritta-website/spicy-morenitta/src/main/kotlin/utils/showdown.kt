@file:JsQualifier("showdown")

package utils

@JsName("Converter")
external class ShowdownConverter() {
	fun makeHtml(text: String): String

	fun setOption(option: String, value: Any)

	fun extension(name: String, exts: Array<ShowdownExtension>)
}