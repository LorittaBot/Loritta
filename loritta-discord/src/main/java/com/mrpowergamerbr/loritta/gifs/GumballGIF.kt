package com.mrpowergamerbr.loritta.gifs

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.LorittaImage
import com.mrpowergamerbr.loritta.utils.enableFontAntiAliasing
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.stream.FileImageOutputStream

object GumballGIF {
	suspend fun getGIF(_toUse: BufferedImage, locale: BaseLocale): File {
		var toUse = BufferedImage(_toUse.width, _toUse.height, BufferedImage.TYPE_INT_ARGB)
		toUse.graphics.drawImage(_toUse, 0, 0, null)
		toUse.graphics.dispose()

		var fileName = Loritta.TEMP + "gumball-" + System.currentTimeMillis() + ".gif"
		var output = FileImageOutputStream(File(fileName))
		val writer = GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, 12, true)

		val gumballHand = readImage(File(Loritta.ASSETS + "gumball/gumball_hand.png"))
		val paper = LorittaImage(toUse)
		paper.resize(400, 280)
		paper.setCorners(
				120f, 1f,
				306f, 38f,
				268f, 278f,
				71f, 235f
		)

		val subtitles = Rectangle(
				0,
				175,
				400,
				50
		)
		var font = toUse.graphics.font.deriveFont(Font.BOLD, 14f)

		for (i in 0..49) {
			val file = File(Loritta.ASSETS + "gumball/gumball_${i.toString().padStart(6, '0')}.png")
			if (file.exists()) {
				var ogImage = readImage(File(Loritta.ASSETS + "gumball/gumball_${i.toString().padStart(6, '0')}.png"))
				var image = BufferedImage(ogImage.width, ogImage.height, BufferedImage.TYPE_INT_ARGB)
				val graphics = image.graphics.enableFontAntiAliasing()

				graphics.font = font
				graphics.color = Color.WHITE
				graphics.drawImage(ogImage, 0, 0, null)

				if (i == 47) {
					graphics.drawImage(paper.bufferedImage, 148, -10, null)
				}

				if (i == 48) {
					graphics.drawImage(paper.bufferedImage, 0, -10, null)
					graphics.drawImage(gumballHand, -4, 0, null)
				}

				if (i == 49) {
					graphics.drawImage(paper.bufferedImage, 4, -10, null)
					graphics.drawImage(gumballHand, 0, 0, null)
					for (i in 0..19) {
						writer.writeToSequence(image)
					}
					break
				}

				if (i in 0..27) {
					ImageUtils.drawCenteredStringOutlined(
							graphics,
							locale["commands.command.gumballliftup.subtitle1"],
							subtitles,
							font
					)
				}
				if (i in 28..45) {
					ImageUtils.drawCenteredStringOutlined(
							graphics,
							locale["commands.command.gumballliftup.subtitle2"],
							subtitles,
							font
					)
				}
				writer.writeToSequence(image)
			}
		}
		writer.close()
		output.close()
		return File(fileName)
	}

}