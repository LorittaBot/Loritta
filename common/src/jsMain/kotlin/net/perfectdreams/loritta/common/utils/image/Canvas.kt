package net.perfectdreams.loritta.common.utils.image

import org.w3c.dom.CanvasRenderingContext2D

@JsName("Canvas")
external class Canvas {
	val width: Int
	val height: Int

	fun getContext(context: String): CanvasRenderingContext2D
}
