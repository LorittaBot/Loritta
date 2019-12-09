package pixi

import pixi.filters.Filter

open external class DisplayObject {
	var x: Double = definedExternally
	var y: Double = definedExternally
	var name: String? = definedExternally
	var alpha: Float = definedExternally
	var filters: Array<Filter>? = definedExternally

	var interactive: Boolean = definedExternally
	var interactiveChildren: Boolean = definedExternally

	fun on(eventName: String, callback: (dynamic) -> (Unit))

	fun destroy()
}