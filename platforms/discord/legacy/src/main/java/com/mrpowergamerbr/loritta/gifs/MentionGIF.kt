package com.mrpowergamerbr.loritta.gifs

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.LorittaImage
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageOutputStream

object MentionGIF {
	suspend fun getGIF(toUse: BufferedImage): File {
		var fileName = Loritta.TEMP + "mention-" + System.currentTimeMillis() + ".gif"
		var output = FileImageOutputStream(File(fileName))
		val writer = GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, 10, true)

		for (i in 0..83) {
			val file = File(Loritta.ASSETS + "mention/mention_${i.toString().padStart(6, '0')}.png")
			if (file.exists()) {
				var ogImage = readImage(File(Loritta.ASSETS + "mention/mention_${i.toString().padStart(6, '0')}.png"))
				var image = BufferedImage(ogImage.width, ogImage.height, BufferedImage.TYPE_INT_ARGB)
				image.graphics.drawImage(ogImage, 0, 0, null)
				if (i in 51..58) {
					val resized = toUse.getScaledInstance(400, 224, BufferedImage.SCALE_SMOOTH).toBufferedImage()
					val transform = LorittaImage(resized)
					transform.setCorners(131F, 55F,
							252F, 16F,
							308F, 186F,
							190F, 223F)

					var overlay = ImageIO.read(File(Loritta.ASSETS + "mention_overlay1.png"))

					image.graphics.drawImage(transform.bufferedImage, 0, 0, null)
					image.graphics.drawImage(overlay, 0, 0, null)
				}
				if (i in 59..65) {
					val resized = toUse.getScaledInstance(500, 400, BufferedImage.SCALE_SMOOTH).toBufferedImage()
					val transform = LorittaImage(resized)
					transform.setCorners(56F, 66F,
							297F, 0F,
							412F, 304F,
							154F, 348F)
					transform.crop(0, 20, 400, 224)

					var overlay = ImageIO.read(File(Loritta.ASSETS + "mention_overlay2.png"))

					image.graphics.drawImage(transform.bufferedImage, 0, 0, null)
					image.graphics.drawImage(overlay, 0, 0, null)
				}
				writer.writeToSequence(image)
			}
		}
		writer.close()
		output.close()
		return File(fileName)
	}

}