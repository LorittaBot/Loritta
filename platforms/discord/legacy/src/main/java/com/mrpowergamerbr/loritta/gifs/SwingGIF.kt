package com.mrpowergamerbr.loritta.gifs

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.LorittaImage
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.stream.FileImageOutputStream

object SwingGIF {
	suspend fun getGIF(_toUse1: BufferedImage, _toUse2: BufferedImage): File {
		var fileName = Loritta.TEMP + "swing-" + System.currentTimeMillis() + ".gif"
		var output = FileImageOutputStream(File(fileName))
		val writer = GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, 6, true)

		val popuko = _toUse1.getScaledInstance(53, 53, BufferedImage.SCALE_SMOOTH)
		val popuko2 = _toUse1.getScaledInstance(165, 165, BufferedImage.SCALE_SMOOTH)
		val popuko3 = _toUse1.getScaledInstance(156, 165, BufferedImage.SCALE_SMOOTH)
		val _pipimi = LorittaImage(_toUse2)
		_pipimi.resize(58, 58)
		val pimpimi2 = _pipimi.copy().bufferedImage
		_pipimi.rotate(10.0)

		val pipimi = _pipimi.bufferedImage

		for (i in 0..54) {
			val file = File(Loritta.ASSETS + "swing/swing_${i.toString().padStart(6, '0')}.png")
			if (file.exists()) {
				var ogImage = readImage(file)
				var image = BufferedImage(ogImage.width, ogImage.height, BufferedImage.TYPE_INT_ARGB)
				val graphics = image.graphics as java.awt.Graphics2D

				graphics.drawImage(ogImage, 0, 0, null)
				if (i in 0..5 || i in 0 + 18..5 + 18 || i in 0 + 18 + 18..5 + 18 + 18) {
					graphics.drawImage(pipimi, 237, 83, null)
				}
				if (i in 6..8 || i in 6 + 18..8 + 18 || i in 6 + 18 + 18..8 + 18  + 18) {
					graphics.drawImage(pipimi, 240, 83, null)
				}
				if (i in 9..17 || i in 9 + 18..17 + 18) {
					graphics.drawImage(pimpimi2, 254, 79, null)
				}

				if (i in 0..8) {
					graphics.drawImage(popuko, 101, 0, null)
				}
				if (i in 9..17) {
					graphics.drawImage(popuko, 87, -10, null)
				}
				if (i in 18..26) {
					graphics.drawImage(popuko, 101, 32, null)
				}
				if (i in 27..35) {
					graphics.drawImage(popuko, 87, 25, null)
				}
				if (i in 36..44) {
					graphics.drawImage(popuko, 101, 73, null)
				}
				if (i in 45..47) {
					graphics.drawImage(popuko2, 123, 5, null)
				}
				if (i in 48..49) {
					graphics.drawImage(popuko3, 123, 24, null)
				}
				if (i in 50..54) {
					graphics.drawImage(popuko3, 83, 24, null)
				}
				writer.writeToSequence(image)
			}
		}
		writer.close()
		output.close()
		return File(fileName)
	}
}