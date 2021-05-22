package net.perfectdreams.loritta.api.utils

import net.perfectdreams.loritta.api.utils.image.Image
import net.perfectdreams.loritta.api.utils.image.JVMImage
import java.awt.image.BufferedImage

actual fun createImage(width: Int, height: Int, imageType: Image.ImageType): Image {
	return JVMImage(BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB))
}