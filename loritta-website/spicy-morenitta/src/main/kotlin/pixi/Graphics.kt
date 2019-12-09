@file:JsQualifier("PIXI")
package pixi

@JsName("Graphics")
open external class Graphics : Container {
	fun lineStyle(a: Int, b: Int, c: Int)
	fun beginFill(color: Int, alpha: Number = definedExternally)
	fun drawRect(x: Int, y: Int, width: Int, height: Int)
	fun drawCircle(x: Int, y: Int, radius: Int)
	fun drawEllipse(x: Int, y: Int, width: Int, height: Int)
	fun endFill()
	fun clear()
	var tint: Int = definedExternally
	var fillAlpha: Number = definedExternally
}