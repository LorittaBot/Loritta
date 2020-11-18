package com.mrpowergamerbr.loritta.gifs

import com.mrpowergamerbr.loritta.Loritta
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.stream.FileImageOutputStream

object GetOverHereGIF {
	suspend fun getGIF(toUse: BufferedImage): File {
		var fileName = Loritta.TEMP + "getoverherescorpion-" + System.currentTimeMillis() + ".gif"
		var output = FileImageOutputStream(File(fileName))
		val writer = GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, 10, true)

		val scaled = toUse.getScaledInstance(50, 50, BufferedImage.SCALE_SMOOTH)
		for (i in 0..52) {
			val file = File(Loritta.ASSETS + "getoverhere/scorpion_${i.toString().padStart(6, '0')}.png")
			if (file.exists()) {
				var ogImage = readImage(File(Loritta.ASSETS + "getoverhere/scorpion_${i.toString().padStart(6, '0')}.png"))
				var image = BufferedImage(ogImage.width, ogImage.height, BufferedImage.TYPE_INT_ARGB)
				image.graphics.drawImage(ogImage, 0, 0, null)
				if (i in 0..4) {
					image.graphics.drawImage(scaled, 9, 27, null)
				}
				if (i == 5) {
					image.graphics.drawImage(scaled, 49, 27, null)
				}
				if (i == 6) {
					image.graphics.drawImage(scaled, 124, 27, null)
				}
				if (i == 7) {
					image.graphics.drawImage(scaled, 107, 27, null)
				}
				if (i in 8..9) {
					image.graphics.drawImage(scaled, 118, 24, null)
				}
				if (i == 10) {
					image.graphics.drawImage(scaled, 85, 12, null)
				}
				writer.writeToSequence(image)
			}
		}
		writer.close()
		output.close()
		return File(fileName)
	}

}