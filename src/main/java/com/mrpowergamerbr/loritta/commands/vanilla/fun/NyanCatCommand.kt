package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import org.apache.commons.lang3.StringUtils
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class NyanCatCommand : CommandBase() {
	override fun getLabel(): String {
		return "nyan"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.NYANCAT_DESCRIPTION.f()
	}

	override fun getUsage(): String {
		return "cat"
	}

	override fun getExample(): List<String> {
		return listOf("", "cat", "caaaaaaat", "caaaaaaaaaaaaat")
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
		var times = 0
		if (context.args.size == 1) {
			times = StringUtils.countMatches(context.args[0], "a")
		}

		val catLeft = ImageIO.read(File(Loritta.FOLDER + "cat_left_v2.png"))
		val catRight = ImageIO.read(File(Loritta.FOLDER + "cat_right_v2.png"))
		val catMiddle = ImageIO.read(File(Loritta.FOLDER + "cat_middle_v2.png"))

		val bi = BufferedImage(catLeft.getWidth(null) + catRight.getWidth(null) + catMiddle.getWidth(null) * times, catLeft.getHeight(null), BufferedImage.TYPE_INT_ARGB)

		var x = 0

		bi.graphics.drawImage(catLeft, x, 0, null)

		x += catLeft.getWidth(null)

		var idx = 0

		while (times > idx) {
			val catMiddleCopy = BufferedImage(catMiddle.width, catMiddle.height, BufferedImage.TYPE_INT_ARGB);
			val graphics = catMiddleCopy.graphics;
			graphics.drawImage(catMiddle, 0, 0, null); // Nós iremos "clonar" o nosso cat middle para colocar alguns pontinhos rosas aleatórios :)
			graphics.color = Color(255, 51, 153); // Usar a cor rosa-meio-roxo que o Nyan Cat tem

			val randomDots = Loritta.random.nextInt(0, 6);

			val invalidDotsPositionsX = ArrayList<Int>();
			val invalidDotsPositionsY = ArrayList<Int>();

			for (i in 0..randomDots) {
				val randomX = Loritta.random.nextInt(0, 2);
				val randomY = Loritta.random.nextInt(2, 16);
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

		context.sendFile(ImageUtils.toBufferedImage(bi.getScaledInstance(bi.width * 4, bi.height * 4, BufferedImage.SCALE_AREA_AVERAGING)), "nyan_cat.png", context.getAsMention(true))
	}
}