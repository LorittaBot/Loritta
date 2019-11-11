@file:JsQualifier("tingle")

package utils

@JsName("modal")
external class TingleModal(options: TingleOptions) {
	fun addFooterBtn(label: String, classes: String, callback: () -> Unit)

	fun setContent(html: Any)

	fun getContent(): String

	fun open()

	fun close()

	fun setFooterContent(html: String)

	fun getFooterContent(): String

	fun checkOverflow()

	fun isOverflow(): Boolean

	fun destroy()
}