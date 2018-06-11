package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class PepeDreamCommand : AbstractCommand("pepedream", listOf("sonhopepe", "pepesonho"), category = CommandCategory.IMAGES) {

	override fun getDescription(locale: BaseLocale): String {
		return locale.get("PEPEDREAM_Description")
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

		val template = ImageIO.read(File(Loritta.ASSETS + "pepedream.png")) // Template
		val base = BufferedImage(400, 320, BufferedImage.TYPE_INT_ARGB)
		val scaled = contextImage.getScaledInstance(100, 100, BufferedImage.SCALE_SMOOTH).toBufferedImage()

		base.graphics.drawImage(scaled, 81, 2, null)
		base.graphics.drawImage(template, 0, 0, null)

		context.sendFile(base, "pepedream.png", context.getAsMention(true));
	}
}