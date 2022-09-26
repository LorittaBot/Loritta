package net.perfectdreams.loritta.common.utils.image

import net.perfectdreams.loritta.common.utils.image.Graphics
import net.perfectdreams.loritta.common.utils.image.Image

class JVMGraphics(val handle: java.awt.Graphics) : Graphics {
	override fun drawImage(image: Image, x: Int, y: Int) {
		image as JVMImage
		handle.drawImage(image.handle, x, y, null)
	}
}