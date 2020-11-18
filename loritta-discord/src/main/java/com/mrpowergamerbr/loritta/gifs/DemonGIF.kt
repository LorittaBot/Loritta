package com.mrpowergamerbr.loritta.gifs

import com.mrpowergamerbr.loritta.Loritta
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageOutputStream

object DemonGIF {
	suspend fun getGIF(_toUse1: BufferedImage, localeId: String): File {
		var fileName = Loritta.TEMP + "demon-" + System.currentTimeMillis() + ".gif"
		var output = FileImageOutputStream(File(fileName))
		val writer = GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, 10, true)

		val popuko = _toUse1.getScaledInstance(75, 75, BufferedImage.SCALE_SMOOTH)
		val popukoHand = readImage(File(Loritta.ASSETS, "demon/popuko_hand1.png"))
		val popukoHand2 = readImage(File(Loritta.ASSETS, "demon/popuko_hand2.png"))
		val popukoHand3 = readImage(File(Loritta.ASSETS, "demon/popuko_hand3.png"))
		val popukoHand4 = readImage(File(Loritta.ASSETS, "demon/popuko_hand4.png"))
		val popukoHand5 = readImage(File(Loritta.ASSETS, "demon/popuko_hand5.png"))

		var lang = "default"
		if (File("${Loritta.ASSETS}demon/${localeId}_begone.png").exists()) {
			lang = localeId
		}

		var begone = readImage(File(Loritta.ASSETS, "demon/${lang}_begone.png"))
		var notYou = readImage(File(Loritta.ASSETS, "demon/${lang}_notyou.png"))
		var notHim = readImage(File(Loritta.ASSETS, "demon/${lang}_nothim.png"))
		var you = readImage(File(Loritta.ASSETS, "demon/${lang}_you.png"))

		for (i in 0..108) {
			val file = File(Loritta.ASSETS + "demon/demon_${i.toString().padStart(6, '0')}.png")
			if (file.exists()) {
				var ogImage = readImage(file)
				var image = BufferedImage(ogImage.width, ogImage.height, BufferedImage.TYPE_INT_ARGB)
				val graphics = image.graphics as java.awt.Graphics2D

				graphics.drawImage(ogImage, 0, 0, null)
				if (i in 2..10) {
					graphics.drawImage(begone, 0, 0, null)
				}
				if (i in 26..40) {
					graphics.drawImage(notYou, 0, 0, null)
				}
				if (i in 48..67) {
					graphics.drawImage(notHim, 0, 0, null)
				}
				if (i in 79..103) {
					graphics.drawImage(you, 0, 0, null)
				}
				if (i in 0..14) {
					graphics.drawImage(popuko, 36, 120, null)
				}
				if (i == 15) {
					graphics.drawImage(popuko, 42, 124, null)
				}
				if (i == 16) {
					graphics.drawImage(popuko, 76, 123, null)
				}
				if (i in 17..18) {
					graphics.drawImage(popuko, 104, 123, null)
				}
				if (i in 19..108) {
					graphics.drawImage(popuko, 128, 123, null)
				}

				if (i in 47..72) {
					graphics.drawImage(popukoHand, 0, 0, null)
				}
				if (i == 73) {
					graphics.drawImage(popukoHand2, 0, 0, null)
				}
				if (i == 74) {
					graphics.drawImage(popukoHand3, 0, 0, null)
				}
				if (i == 75 || i == 76) {
					graphics.drawImage(popukoHand4, 0, 0, null)
				}
				if (i in 77..108) {
					graphics.drawImage(popukoHand5, 0, 0, null)
				}
				writer.writeToSequence(image)
			}
		}
		writer.close()
		output.close()
		return File(fileName)
	}
}