@file:JsQualifier("PIXI")
package pixi

@JsName("TilingSprite")
open external class TilingSprite(texture: Any, width: Number, height: Number) : Sprite {
	var tilePosition: TilePosition = definedExternally
}