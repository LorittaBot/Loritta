package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class DeusCommand : AbstractCommand("god", listOf("deus"), CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["DEUS_DESCRIPTION"]
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
		var contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		var template = ImageIO.read(File(Loritta.ASSETS + "deus.png")); // Template

		var scaled = contextImage.getScaledInstance(87, 87, BufferedImage.SCALE_SMOOTH)
		template.graphics.drawImage(scaled, 1, 1, null);

		context.sendFile(template, "deus.png", context.getAsMention(true));
	}
}