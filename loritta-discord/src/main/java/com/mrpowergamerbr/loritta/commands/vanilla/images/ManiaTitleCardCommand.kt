package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class ManiaTitleCardCommand : AbstractCommand("maniatitlecard", category = CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.maniatitlecard.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.maniatitlecard.examples")
	override fun getUsage() = arguments {
		argument(ArgumentType.TEXT) {}
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val split = context.args.joinToString(" ").split("|").onEach { it.trim() }
			val text1 = split[0]
			val text2 = if (split.size > 1) {
				split[1]
			} else {
				""
			}
			context.sendFile(generateTitleCard(text1, text2), "mania_title_card.png", context.getAsMention(true))
		} else {
			this.explain(context)
		}
	}

	fun generateTitleCard(text1: String = "", text2: String = ""): BufferedImage {
		val image = ImageIO.read(File(Loritta.ASSETS, "sonic_mania_title_card.png"))
		val left = ImageIO.read(File(Loritta.ASSETS, "mania_font/cut_left.png"))
		val right = ImageIO.read(File(Loritta.ASSETS, "mania_font/cut_right.png"))

		val graphics = image.graphics
		graphics.color = Color.BLACK

		val bottomText = if (text2.isNotEmpty()) {
			text2
		} else {
			text1
		}

		val topText = if (text2.isEmpty()) {
			""
		} else {
			text1
		}

		run {
			var x = 516

			var y = 336 - 60

			graphics.drawImage(right, x, y + right.height, null)


			for (c in bottomText.reversed()) {
				if (c == ' ') {
					graphics.fillRect(x - 26, y + 34, 26 + 2, 34)
					x -= 26
					continue
				}
				val charFile = File(Loritta.ASSETS, "mania_font/${c.toLowerCase()}.png")

				if (charFile.exists()) {
					val charImg = ImageIO.read(charFile)

					val scaledCharImg = charImg.getScaledInstance(charImg.width * 2, charImg.height * 2, BufferedImage.SCALE_FAST).toBufferedImage()

					graphics.fillRect(x - scaledCharImg.width, y + 34, scaledCharImg.width + 2, 34)
					graphics.drawImage(scaledCharImg, x - scaledCharImg.width, y, null)
					x -= scaledCharImg.width + 2
				}
			}
			graphics.drawImage(left, x - left.width + 2, y + left.height, null)
		}

		if (topText.isNotEmpty()) {
			var x = 516

			var y = 257 - 60

			graphics.drawImage(right, x, y + right.height, null)

			for (c in topText.reversed()) {
				if (c == ' ') {
					graphics.fillRect(x - 26, y + 34, 26 + 2, 34)
					x -= 26
					continue
				}
				val charFile = File(Loritta.ASSETS, "mania_font/${c.toLowerCase()}.png")

				if (charFile.exists()) {
					val charImg = ImageIO.read(charFile)

					val scaledCharImg = charImg.getScaledInstance(charImg.width * 2, charImg.height * 2, BufferedImage.SCALE_FAST).toBufferedImage()

					graphics.fillRect(x - scaledCharImg.width, y + 34, scaledCharImg.width + 2, 34)
					graphics.drawImage(scaledCharImg, x - scaledCharImg.width, y, null)
					x -= scaledCharImg.width + 2
				}
			}
			graphics.drawImage(left, x - left.width + 2, y + left.height, null)
		}

		return image
	}
}