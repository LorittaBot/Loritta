package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class PerfeitoCommand : AbstractCommand("perfect", listOf("perfeito"), CommandCategory.IMAGES) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["PERFEITO_DESCRIPTION"]
	}

	override fun getExamples(): List<String> {
		return listOf("@Loritta")
	}

	override fun getUsage(): String {
		return "<imagem>"
	}

	override fun needsToUploadFiles() = true

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val contextImage = context.getImageAt(0, avatarSize = 256) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val template = ImageIO.read(File(Loritta.ASSETS + "perfeito.png")) // Template

		val scaled = contextImage.getScaledInstance(231, 231, BufferedImage.SCALE_SMOOTH)
		template.graphics.drawImage(scaled, 225, 85, null)

		context.sendFile(template, "perfeito.png", context.getAsMention(true))
	}
}