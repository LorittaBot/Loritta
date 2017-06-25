package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

class QuadroCommand : CommandBase() {
	override fun getLabel(): String {
		return "quadro"
	}

	override fun getAliases(): List<String> {
		return listOf("frame", "wolverine")
	}

	override fun getDescription(): String {
		return "Coloca alguém em um quadro com o Wolverine olhando para ele";
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN;
	}

	override fun run(context: CommandContext) {
		var contextImage = LorittaUtils.getImageFromContext(context, 0);
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return;
		}
		var template = ImageIO.read(File(Loritta.FOLDER + "wolverine.png")); // Template
		var image = BufferedImage(600, 872, BufferedImage.TYPE_INT_ARGB)

		var graphics = image.graphics;
		var skewed = javaxt.io.Image(contextImage);

		skewed.resize(371, 371);

		skewed.width = 390; // Aumentar o tamanho da imagem para manipular ela
		skewed.height = 390;
		// skew image
		skewed.setCorners(
				// keep the upper left corner as it is
				0F,3F, // UL

				// push the upper right corner more to the bottom
				280F,0F , // UR

				// push the lower right corner more to the left
				383F,383F, // LR

				// push the lower left corner more to the right
				20F, 389F); // LL

		graphics.drawImage(skewed.bufferedImage, 163, 471, null);

		graphics.drawImage(template, 0, 0, null); // Desenhe o template por cima!

		val os = ByteArrayOutputStream()
		ImageIO.write(image, "png", os)
		val inputStream = ByteArrayInputStream(os.toByteArray())

		context.sendFile(inputStream, "quadro.png", context.getAsMention(true));
	}
}