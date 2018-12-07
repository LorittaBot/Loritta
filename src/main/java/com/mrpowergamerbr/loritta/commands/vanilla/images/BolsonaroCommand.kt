package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaImage
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class BolsonaroCommand : AbstractCommand("bolsonaro", category = CommandCategory.IMAGES) {
	companion object {
		val TEMPLATE by lazy { ImageIO.read(File(Constants.ASSETS_FOLDER, "bolsonaro_tv.png")) }
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.format { commands.entertainment.bolsonaro.description }
	}

	override fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.IMAGE) {}
		}
	}

	override fun getExamples(): List<String> {
		return listOf("@Loritta")
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val image = BufferedImage(400, 230, BufferedImage.TYPE_INT_ARGB)

		val graphics = image.graphics
		val skewed = LorittaImage(contextImage)

		skewed.resize(400, 230)

		// skew image
		skewed.setCorners(
				// keep the upper left corner as it is
				108F,11F, // UL

				383F,8F, // UR

				375F,167F, // LR

				106F, 158F) // LL

		graphics.drawImage(skewed.bufferedImage, 0, 0, null)

		graphics.drawImage(TEMPLATE, 0, 0, null) // Desenhe o template por cima!

		context.sendFile(image, "bolsonaro_tv.png", context.getAsMention(true))
	}
}