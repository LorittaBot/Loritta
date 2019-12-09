@file:JsQualifier("PIXI")
package pixi

@JsName("Sprite")
open external class Sprite(texture: Any) : Container {
	var rotation: Double = definedExternally
	var position: Position = definedExternally
	var scale: Scale = definedExternally
	var anchor: Anchor = definedExternally
	var pivot: Pivot = definedExternally
	var tint: Int = definedExternally

	var texture: Texture = definedExternally
}