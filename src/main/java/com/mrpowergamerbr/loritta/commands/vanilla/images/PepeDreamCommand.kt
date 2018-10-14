package com.mrpowergamerbr.loritta.commands.vanilla.images

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
	companion object {
		val TEMPLATE by lazy { ImageIO.read(File(Constants.ASSETS_FOLDER, "pepedream.png")) }
	}

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

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val base = BufferedImage(400, 320, BufferedImage.TYPE_INT_ARGB)
		val scaled = contextImage.getScaledInstance(100, 100, BufferedImage.SCALE_SMOOTH).toBufferedImage()

		base.graphics.drawImage(scaled, 81, 2, null)
		base.graphics.drawImage(TEMPLATE, 0, 0, null)

		context.sendFile(base, "pepe_dream.png", context.getAsMention(true));
	}
}