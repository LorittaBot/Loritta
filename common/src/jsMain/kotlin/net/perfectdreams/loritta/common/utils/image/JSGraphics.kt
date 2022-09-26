package net.perfectdreams.loritta.common.utils.image

import net.perfectdreams.loritta.common.utils.image.Graphics
import net.perfectdreams.loritta.common.utils.image.Image
import org.w3c.dom.CanvasRenderingContext2D

class JSGraphics(val context: CanvasRenderingContext2D) : Graphics {
	override fun drawImage(image: Image, x: Int, y: Int) {
		image as JSImage
		context.drawImage(image.canvas.getContext("2d").canvas, x.toDouble(), y.toDouble())
	}
}