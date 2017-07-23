package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaImage
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class QuadroCommand : CommandBase() {
	override fun getLabel(): String {
		return "quadro"
	}

	override fun getAliases(): List<String> {
		return listOf("frame", "wolverine")
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.QUADRO_DESCRIPTION.f();
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN;
	}

	override fun getUsage(): String {
		return "<imagem>";
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
		var contextImage = LorittaUtils.getImageFromContext(context, 0, 25, 256);
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return;
		}
		var template = ImageIO.read(File(Loritta.FOLDER + "wolverine.png")); // Template
		var image = BufferedImage(206, 300, BufferedImage.TYPE_INT_ARGB)

		var graphics = image.graphics;
		var skewed = LorittaImage(contextImage);

		skewed.resize(206, 300);

		// skew image
		skewed.setCorners(
				// keep the upper left corner as it is
				55F,165F, // UL

				152F,159F, // UR

				172F,283F, // LR

				73F, 293F); // LL

		graphics.drawImage(skewed.bufferedImage, 0, 0, null);

		graphics.drawImage(template, 0, 0, null); // Desenhe o template por cima!

		context.sendFile(image, "quadro.png", context.getAsMention(true));
	}
}