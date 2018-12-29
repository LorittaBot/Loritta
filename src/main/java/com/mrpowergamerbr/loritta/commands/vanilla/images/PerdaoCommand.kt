package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class PerdaoCommand : AbstractCommand("perdao", listOf("perdão"), CommandCategory.IMAGES) {
	companion object {
		val TEMPLATE by lazy { ImageIO.read(File(Constants.ASSETS_FOLDER, "perdao.png")) }
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["PERDAO_DESCRIPTION"]
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

		// RULE OF THREE!!11!
		// larguraOriginal - larguraDoContextImage
		// alturaOriginal - X
		val newHeight = (contextImage.width * TEMPLATE.height) / TEMPLATE.width

		val scaledTemplate = TEMPLATE.getScaledInstance(contextImage.width, Math.max(newHeight, 1), BufferedImage.SCALE_SMOOTH)
		contextImage.graphics.drawImage(scaledTemplate, 0, contextImage.height - scaledTemplate.getHeight(null), null)

		context.sendFile(contextImage, "perdao.png", context.getAsMention(true))
	}
}