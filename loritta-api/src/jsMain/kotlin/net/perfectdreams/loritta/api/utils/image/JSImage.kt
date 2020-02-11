package net.perfectdreams.loritta.api.utils.image

import nodecanvas.Buffer
import nodecanvas.createCanvas
import nodecanvas.toByteArray

class JSImage(val canvas: Canvas) : Image {
	override val width: Int
		get() = canvas.width
	override val height: Int
		get() = canvas.height

	override fun getScaledInstance(width: Int, height: Int, scaleType: Image.ScaleType): Image {
		// Clonar imagem original
		val scaledImage = createCanvas(width, height)
		val ctx = scaledImage.getContext("2d")
		ctx.drawImage(this.canvas.getContext("2d").canvas, 0.0, 0.0, width.toDouble(), height.toDouble())

		return JSImage(scaledImage)
	}

	override fun getSkewedInstance(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Image {
		return JSImage(
				HackySkew(canvas)
						.setCorners(
								x0, y0,
								x1, y1,
								x2, y2,
								x3, y3
						)
		)
	}

	override fun createGraphics(): Graphics {
		return JSGraphics(canvas.getContext("2d"))
	}

	override fun toByteArray(): ByteArray {
		val ctx = canvas.getContext("2d")
		val dataUrl = ctx.canvas.toDataURL("image/png")
		val dataBase64 = dataUrl.split("base64,").last()
		val buf = Buffer.from(dataBase64, "base64")

		return buf.toByteArray()
	}
}