package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import org.apache.commons.lang3.StringUtils
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class NyanCatCommand : AbstractCommand("nyan", category = CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("NYANCAT_DESCRIPTION")
	}

	override fun getUsage(): String {
		return "cat"
	}

	override fun getExample(): List<String> {
		return listOf("", "cat", "caaaaaaat", "caaaaaaaaaaaaat", "dog")
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var times = 0
		var isDog = false
		if (context.args.size == 1) {
			var nonRepeatedCharsMessage = context.args[0].replace(Regex("(.)\\1{1,}"), "$1")
			isDog = nonRepeatedCharsMessage.equals("dog", true)
			times = StringUtils.countMatches(context.args[0], if (isDog) "o" else "a")
		}

		val catLeft = if (!isDog) {
			ImageIO.read(File(Loritta.ASSETS + "cat_left_v2.png"))
		} else {
			ImageIO.read(File(Loritta.ASSETS + "dog_left.png"))
		}
		val catRight = if (!isDog) {
			ImageIO.read(File(Loritta.ASSETS + "cat_right_v2.png"))
		} else {
			ImageIO.read(File(Loritta.ASSETS + "dog_right.png"))
		}
		val catMiddle = if (!isDog) {
			ImageIO.read(File(Loritta.ASSETS + "cat_middle_v2.png"))
		} else {
			ImageIO.read(File(Loritta.ASSETS + "dog_middle.png"))
		}

		val bi = BufferedImage(catLeft.getWidth(null) + catRight.getWidth(null) + catMiddle.getWidth(null) * times, catLeft.getHeight(null), BufferedImage.TYPE_INT_ARGB)

		var x = 0

		bi.graphics.drawImage(catLeft, x, 0, null)

		x += catLeft.getWidth(null)

		var idx = 0

		while (times > idx) {
			val catMiddleCopy = BufferedImage(catMiddle.width, catMiddle.height, BufferedImage.TYPE_INT_ARGB);
			val graphics = catMiddleCopy.graphics;
			graphics.drawImage(catMiddle, 0, 0, null); // Nós iremos "clonar" o nosso cat middle para colocar alguns pontinhos rosas aleatórios :)
			graphics.color = if (!isDog) {
				Color(255, 51, 153) // Usar a cor rosa-meio-roxo que o Nyan Cat tem
			} else {
				Color(243, 254, 255)
			}

			val randomDots = Loritta.RANDOM.nextInt(0, 6);

			val invalidDotsPositionsX = ArrayList<Int>();
			val invalidDotsPositionsY = ArrayList<Int>();

			for (i in 0..randomDots) {
				val randomX = Loritta.RANDOM.nextInt(0, 2);
				val randomY = Loritta.RANDOM.nextInt(2, 16);
				if (invalidDotsPositionsX.contains(randomX) || invalidDotsPositionsX.contains(randomX - 1) || invalidDotsPositionsX.contains(randomX + 1)) {
					continue;
				}

				if (invalidDotsPositionsY.contains(randomY) || invalidDotsPositionsY.contains(randomY - 1) || invalidDotsPositionsY.contains(randomY + 1)) {
					continue;
				}

				// Sabia que width/height 0 = 1px? Agora você sabe!
				graphics.drawRect(randomX, randomY, 0, 0); // Faça um retângulo 1x1 na nossa coordenada aleatória

				invalidDotsPositionsX.add(randomX);
				invalidDotsPositionsY.add(randomY);
			}

			invalidDotsPositionsX.clear();
			invalidDotsPositionsY.clear();

			bi.graphics.drawImage(catMiddleCopy, x, 0, null) // E depois desenhe nossa imagem modificada na imagem original
			x += catMiddleCopy.getWidth(null)
			idx++
		}

		bi.graphics.drawImage(catRight, x, 0, null)

		if (isDog) { // Desenhar as orelhas do dog
			val dogEars = ImageIO.read(File(Loritta.ASSETS + "dog_ears.png"))

			bi.graphics.drawImage(dogEars, bi.width - 21, 5, null)
		}

		context.sendFile(ImageUtils.toBufferedImage(bi.getScaledInstance(bi.width * 4, bi.height * 4, BufferedImage.SCALE_AREA_AVERAGING)), "nyan_cat.png", context.getAsMention(true))
	}
}