package net.perfectdreams.loritta.legacy.api.utils

import net.perfectdreams.loritta.legacy.api.utils.image.Image
import net.perfectdreams.loritta.legacy.api.utils.image.JSImage
import nodecanvas.createCanvas

actual fun createImage(width: Int, height: Int, imageType: Image.ImageType): Image {
	return JSImage(createCanvas(width, height))
}