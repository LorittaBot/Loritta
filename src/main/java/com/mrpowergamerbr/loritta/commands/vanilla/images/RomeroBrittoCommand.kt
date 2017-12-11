package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaImage
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class RomeroBrittoCommand : CommandBase("romerobritto", listOf("pintura", "painting")) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["ROMEROBRITTO_DESCRIPTION"];
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.IMAGES
	}

	override fun getUsage(): String {
		return "<imagem>";
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var contextImage = LorittaUtils.getImageFromContext(context, 0, 25, 256);
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return;
		}
		var template = ImageIO.read(File(Loritta.ASSETS + "romero_britto.png")); // Template
		var image = BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB)

		var graphics = image.graphics;
		var skewed = LorittaImage(contextImage);

		skewed.resize(300, 300);

		// skew image
		skewed.setCorners(
				// keep the upper left corner as it is
				16F,19F, // UL

				201F,34F, // UR

				208F,218F, // LR

				52F, 294F); // LL

		graphics.drawImage(skewed.bufferedImage, 0, 0, null);

		graphics.drawImage(template, 0, 0, null); // Desenhe o template por cima!

		context.sendFile(image, "romero_britto.png", context.getAsMention(true));
	}
}