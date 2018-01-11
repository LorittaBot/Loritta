package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class PerdaoCommand : AbstractCommand("perdao", listOf("perdão"), CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["PERDAO_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta");
	}

	override fun getUsage(): String {
		return "<imagem>";
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var contextImage = LorittaUtils.getImageFromContext(context, 0);
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return;
		}
		var template = ImageIO.read(File(Loritta.ASSETS + "perdao.png")); // Template

		// RULE OF THREE!!11!
		// larguraOriginal - larguraDoContextImage
		// alturaOriginal - X
		var newHeight = (contextImage.width * template.height) / template.width

		var scaledTemplate = template.getScaledInstance(contextImage.width, Math.max(newHeight, 1), BufferedImage.SCALE_SMOOTH)
		contextImage.graphics.drawImage(scaledTemplate, 0, contextImage.height - scaledTemplate.getHeight(null), null);

		context.sendFile(contextImage, "perdao.png", context.getAsMention(true));
	}
}