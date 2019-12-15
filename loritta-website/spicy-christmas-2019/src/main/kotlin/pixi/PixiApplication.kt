@file:JsQualifier("PIXI")
package pixi

import org.w3c.dom.HTMLCanvasElement

@JsName("Application")
external class PixiApplication(options: ApplicationOptions) {
	val view: HTMLCanvasElement = definedExternally
	val renderer: SystemRenderer = definedExternally
	var stage: Container = definedExternally
	val ticker: Ticker
}