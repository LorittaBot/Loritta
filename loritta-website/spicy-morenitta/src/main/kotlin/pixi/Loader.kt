@file:JsQualifier("PIXI")
package pixi

@JsName("loader")
external class Loader {
	val resources: dynamic = definedExternally

	fun add(path: String): Loader

	fun load(callback: (Any, Any) -> Unit)
}