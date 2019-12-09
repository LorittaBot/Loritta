@file:JsQualifier("PIXI")
package pixi

@JsName("Container")
open external class Container : DisplayObject {
	var width: Int = definedExternally
	var height: Int = definedExternally
	var visible: Boolean = definedExternally
	var children: Array<DisplayObject> = definedExternally

	fun addChild(child: Any)
	fun addChildAt(child: Any, number: Number)
	fun removeChild(child: Any)
	fun removeChildren()
}