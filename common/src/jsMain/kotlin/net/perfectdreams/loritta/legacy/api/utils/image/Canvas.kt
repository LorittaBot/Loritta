package net.perfectdreams.loritta.legacy.api.utils.image

import org.w3c.dom.CanvasRenderingContext2D

@JsName("Canvas")
external class Canvas {
	val width: Int
	val height: Int

	fun getContext(context: String): CanvasRenderingContext2D
}
