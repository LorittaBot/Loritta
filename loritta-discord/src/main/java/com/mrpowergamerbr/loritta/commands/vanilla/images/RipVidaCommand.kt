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

class RipVidaCommand : AbstractCommand("riplife", listOf("ripvida"), CommandCategory.IMAGES) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.get("RIPVIDA_DESCRIPTION")
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

		val template = ImageIO.read(File(Loritta.ASSETS + context.legacyLocale.get("RIPVIDA_FILE"))) // Template

		val scaled = contextImage.getScaledInstance(133, 133, BufferedImage.SCALE_SMOOTH)
		template.graphics.drawImage(scaled, 133, 0, null)

		context.sendFile(template, context.legacyLocale.get("RIPVIDA_FILE"), context.getAsMention(true))
	}
}