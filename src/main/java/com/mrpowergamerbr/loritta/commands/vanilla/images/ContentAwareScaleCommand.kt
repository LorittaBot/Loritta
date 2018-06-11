package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaImage
import com.mrpowergamerbr.loritta.utils.SeamCarver
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class ContentAwareScaleCommand : AbstractCommand("contentawarescale", listOf("cas", "contentaware", "seamcarver"), category = CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["CONTENTAWARESCALE_Description"]
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta")
	}

	override fun getUsage(): String {
		return "<imagem>"
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val loriImage = LorittaImage(contextImage)
		loriImage.resize(512, 512, true)
		contextImage = loriImage.bufferedImage

		var newImage = contextImage

		for (i in 0..399) {
			// determine scale
			var scaleTo = if (Loritta.RANDOM.nextBoolean()) "horizontal" else "vertical"

			if (200 > newImage.height) { // se ficar menos que 200 irá ficar bem ruim a imagem
				scaleTo = "vertical"
			}

			if (200 > newImage.width) { // se ficar menos que 200 irá ficar bem ruim a imagem
				scaleTo = "horizontal"
			}

			if (100 > newImage.height) { // se ficar menos que 200 irá ficar bem ruim a imagem
				break
			}

			if (100 > newImage.width) { // se ficar menos que 200 irá ficar bem ruim a imagem
				break
			}

			// Get the new image w/o one seam.
			newImage = SeamCarver.carveSeam(newImage, scaleTo)
		}

		context.sendFile(newImage, "content_aware_scale.png", context.getAsMention(true))
	}
}