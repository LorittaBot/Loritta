package net.perfectdreams.loritta.common.utils

import net.perfectdreams.loritta.common.utils.image.Image
import net.perfectdreams.loritta.common.utils.image.JSImage
import nodecanvas.createCanvas

actual fun createImage(width: Int, height: Int, imageType: Image.ImageType): Image {
	return JSImage(createCanvas(width, height))
}