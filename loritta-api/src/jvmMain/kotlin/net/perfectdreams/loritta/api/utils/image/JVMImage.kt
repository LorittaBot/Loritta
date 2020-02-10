package net.perfectdreams.loritta.api.utils.image

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class JVMImage(val handle: java.awt.Image) : Image {
	override fun getScaledInstance(width: Int, height: Int, scaleType: Image.ScaleType): JVMImage {
		val hints = when (scaleType) {
			Image.ScaleType.SMOOTH -> java.awt.Image.SCALE_SMOOTH
		}
		return JVMImage(handle.getScaledInstance(width, height, hints))
	}

	override fun createGraphics(): Graphics {
		return JVMGraphics(handle.graphics)
	}

	override fun toByteArray(): ByteArray {
		val output = object : ByteArrayOutputStream() {
			@Synchronized
			override fun toByteArray(): ByteArray {
				return this.buf
			}
		}

		val bufferedImage = if (handle !is BufferedImage)
			toBufferedImage(handle)
		else
			handle

		ImageIO.write(bufferedImage, "png", output)

		return output.toByteArray()
	}

	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	private fun toBufferedImage(img: java.awt.Image): BufferedImage {
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
}