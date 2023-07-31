@file:JsModule("pixi.js")
@file:JsNonModule
package net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi

import org.w3c.dom.HTMLCanvasElement

external class Application(options: dynamic) {
    val view: HTMLCanvasElement = definedExternally
    val stage: PixiStage = definedExternally
    val ticker: PixiTicker = definedExternally

    fun destroy()
}

@JsName("Sprite")
open external class PixiSprite(texture: PixiTexture) : DisplayObject {
    var anchor: PixiAnchor
    var texture: PixiTexture
}

@JsName("Texture")
external class PixiTexture

external class PixiAnchor {
    var x: Double
    var y: Double
}

external class PixiScale {
    var x: Double
    var y: Double
}

external class PixiStage {
    fun addChild(sprite: DisplayObject)
    fun removeChild(sprite: DisplayObject)
}

external class PixiTicker {
    fun add(block: (Double) -> (Unit))

    var maxFPS: Int
}

external class PixiSpriteLoader {
    fun from(input: String): PixiSprite
}

external class PixiTextureLoader {
    fun from(input: String): PixiTexture
}

external class PixiSettings {
    var TARGET_FPMS: Double
}

external class Text(text: String, style: dynamic) : PixiSprite

open external class DisplayObject {
    var x: Double
    var y: Double
    var scale: PixiScale
    var width: Double
    var height: Double
    var angle: Int
}

external class Container : DisplayObject {
    fun addChild(sprite: DisplayObject)
    fun removeChild(sprite: DisplayObject)
}