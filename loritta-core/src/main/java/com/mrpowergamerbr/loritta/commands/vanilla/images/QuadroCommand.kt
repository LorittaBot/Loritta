package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaImage
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class QuadroCommand : AbstractCommand("frame", listOf("quadro", "wolverine"), CommandCategory.IMAGES) {
	companion object {
		val TEMPLATE by lazy { ImageIO.read(File(Constants.ASSETS_FOLDER, "wolverine.png")) }
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["QUADRO_DESCRIPTION"]
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

		val image = BufferedImage(206, 300, BufferedImage.TYPE_INT_ARGB)

		val graphics = image.graphics
		val skewed = LorittaImage(contextImage)

		skewed.resize(206, 300)

		// skew image
		skewed.setCorners(
				// keep the upper left corner as it is
				55F,165F, // UL

				152F,159F, // UR

				172F,283F, // LR

				73F, 293F) // LL

		graphics.drawImage(skewed.bufferedImage, 0, 0, null)

		graphics.drawImage(TEMPLATE, 0, 0, null) // Desenhe o template por cima!

		context.sendFile(image, "quadro.png", context.getAsMention(true))
	}
}