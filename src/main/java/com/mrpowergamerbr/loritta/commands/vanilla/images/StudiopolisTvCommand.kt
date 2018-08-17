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

class StudiopolisTvCommand : AbstractCommand("studiopolistv", category = CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("STUDIOPOLISTV_DESCRIPTION")
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
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val template = ImageIO.read(File(Loritta.ASSETS + "studiopolis.png")); // Template

		val scaled = contextImage.getScaledInstance(190, 115, BufferedImage.SCALE_SMOOTH)
		template.graphics.drawImage(scaled, 154, 61, null);

		context.sendFile(template, "studiopolis_tv.png", context.getAsMention(true));
	}
}