package net.perfectdreams.loritta.api.utils.image

class JVMGraphics(val handle: java.awt.Graphics) : Graphics {
	override fun drawImage(image: Image, x: Int, y: Int) {
		image as JVMImage
		handle.drawImage(image.handle, x, y, null)
	}
}