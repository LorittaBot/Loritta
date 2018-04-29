package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaImage
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class LicencaCommand : AbstractCommand("license", listOf("licença", "licenca"), CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["QUADRO_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta Licença para ser incrível!");
	}

	override fun getUsage(): String {
		return "<usuário> <texto>";
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val contextImage = LorittaUtils.getImageFromContext(context, 0, 25, 256);
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return
		}
		var template = ImageIO.read(File(Loritta.ASSETS + "licenca.png")); // Template
		var image = BufferedImage(400, 295, BufferedImage.TYPE_INT_ARGB)

		var graphics = image.graphics
		var skewed = LorittaImage(contextImage)

		skewed.resize(400, 295)

		// skew image
		skewed.setCorners(
				// keep the upper left corner as it is
				190f,50f, // UL

				345f,20f, // UR

				381f,181f, // LR

				223f, 211f); // LL

		graphics.drawImage(template, 0, 0, null); // Desenhe o template por cima!

		graphics.drawImage(skewed.bufferedImage, 0, 0, null)

		context.sendFile(image, "license.png", context.getAsMention(true));
	}
}