package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaImage
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class RomeroBrittoCommand : AbstractCommand("romerobritto", listOf("pintura", "painting"), CommandCategory.IMAGES) {
	companion object {
		val TEMPLATE by lazy { ImageIO.read(File(Constants.ASSETS_FOLDER, "romero_britto.png")) }
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["ROMEROBRITTO_DESCRIPTION"]
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

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val contextImage = context.getImageAt(0, 25, 256) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val image = BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB)

		val graphics = image.graphics
		val skewed = LorittaImage(contextImage)

		skewed.resize(300, 300)

		// skew image
		skewed.setCorners(
				// keep the upper left corner as it is
				16F,19F, // UL

				201F,34F, // UR

				208F,218F, // LR

				52F, 294F) // LL

		graphics.drawImage(skewed.bufferedImage, 0, 0, null)

		graphics.drawImage(TEMPLATE, 0, 0, null) // Desenhe o template por cima!

		context.sendFile(image, "romero_britto.png", context.getAsMention(true))
	}
}