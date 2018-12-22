package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class SustoCommand : AbstractCommand("fright", listOf("susto"), CommandCategory.IMAGES) {
	companion object {
		val TEMPLATE by lazy { ImageIO.read(File(Constants.ASSETS_FOLDER, "loritta_susto.png")) }
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["SUSTO_Description"]
	}

	override fun getExamples(): List<String> {
		return listOf("@Loritta")
	}

	override fun getUsage(): String {
		return "<imagem>"
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val base = BufferedImage(191, 300, BufferedImage.TYPE_INT_ARGB)
		val scaled = contextImage.getScaledInstance(84, 63, BufferedImage.SCALE_SMOOTH)
		base.graphics.drawImage(scaled, 61, 138, null)
		base.graphics.drawImage(TEMPLATE, 0, 0, null)

		context.sendFile(base, "loritta_susto.png", context.getAsMention(true))
	}
}