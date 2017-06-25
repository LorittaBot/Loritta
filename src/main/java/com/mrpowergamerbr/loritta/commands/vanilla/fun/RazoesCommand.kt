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

class RazoesCommand : CommandBase() {
	override fun getLabel(): String {
		return "razoes"
	}

	override fun getAliases(): List<String> {
		return listOf("razões", "reasons")
	}

	override fun getDescription(): String {
		return "Qual é a sua razão para viver?";
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
		var template = ImageIO.read(File(Loritta.FOLDER + "reasons.png")); // Template
		var image = BufferedImage(346, 600, BufferedImage.TYPE_INT_ARGB)

		var graphics = image.graphics;
		var skewed = javaxt.io.Image(contextImage);

		skewed.resize(202, 202);
		skewed.width = 240; // Aumentar o tamanho da imagem para manipular ela
		skewed.height = 240;
		// skew image
		skewed.setCorners(
				// keep the upper left corner as it is
				0F,0F, // UL

				// push the upper right corner more to the bottom
				202 - 40F,40F , // UR

				// push the lower right corner more to the left
				236F,210F, // LR

				// push the lower left corner more to the right
				95F, 215F); // LL

		graphics.drawImage(skewed.bufferedImage, 30, 370, null);

		graphics.drawImage(template, 0, 0, null); // Desenhe o template por cima!

		val os = ByteArrayOutputStream()
		ImageIO.write(image, "png", os)
		val inputStream = ByteArrayInputStream(os.toByteArray())

		context.sendFile(inputStream, "reasons.png", context.getAsMention(true));
	}
}