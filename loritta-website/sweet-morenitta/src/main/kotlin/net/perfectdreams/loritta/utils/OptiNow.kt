package net.perfectdreams.loritta.utils

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun toBufferedImage(img: Image): BufferedImage {
	if (img is BufferedImage) {
		return img
	}

	// Create a buffered image with transparency
	val bimage = BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB)

	// Draw the image on to the buffered image
	val bGr = bimage.createGraphics()
	bGr.drawImage(img, 0, 0, null)
	bGr.dispose()

	// Return the buffered image
	return bimage
}

fun main() {
	val inputFile = File("C:\\Users\\leona\\Documents\\LorittaAssets\\website\\static\\assets\\img\\home\\lori_notification.png")
	val input = ImageIO.read(
			inputFile
	)

	val minimum = 200
	var currentWidthTarget = input.width

	do {
		currentWidthTarget -= 100
		val currentHeightTarget = (input.height * currentWidthTarget) / input.width

		println("Targeting ${currentWidthTarget}x$currentHeightTarget")
		val scaled = input.getScaledInstance(currentWidthTarget, currentHeightTarget, BufferedImage.SCALE_SMOOTH)

		ImageIO.write(toBufferedImage(scaled), "png", File("C:\\Users\\leona\\Documents\\LorittaAssets\\website\\static\\assets\\img\\home\\", inputFile.nameWithoutExtension + "_${currentWidthTarget}w.png"))
	} while (currentWidthTarget >= minimum)

}