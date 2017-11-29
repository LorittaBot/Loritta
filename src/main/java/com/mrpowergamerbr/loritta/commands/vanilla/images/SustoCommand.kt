package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class SustoCommand : CommandBase("susto") {
	override fun getDescription(locale: BaseLocale): String {
		return locale["SUSTO_Description"]
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
		var contextImage = LorittaUtils.getImageFromContext(context, 0);

		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return;
		}
		val base = BufferedImage(191, 300, BufferedImage.TYPE_INT_ARGB)
		var scaled = contextImage.getScaledInstance(84, 63, BufferedImage.SCALE_SMOOTH)
		base.graphics.drawImage(scaled, 61, 138, null);
		var template = ImageIO.read(File(Loritta.ASSETS + "loritta_susto.png")); // Template
		base.graphics.drawImage(template, 0, 0, null)

		context.sendFile(base, "loritta_susto.png", context.getAsMention(true));
	}
}