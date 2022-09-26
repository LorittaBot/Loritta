package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.common.utils.LorittaImage
import net.perfectdreams.loritta.morenitta.utils.SeamCarver
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.morenitta.api.commands.Command
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.LorittaBot

class ContentAwareScaleCommand(loritta: LorittaBot) : AbstractCommand(loritta, "contentawarescale", listOf("cas", "contentaware", "seamcarver"), category = net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.contentawarescale.description")
	override fun getExamplesKey() = Command.SINGLE_IMAGE_EXAMPLES_KEY
	override fun getUsage() = arguments {
		argument(ArgumentType.IMAGE) {}
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		var contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val loriImage = LorittaImage(contextImage)
		loriImage.resize(256, 256, true)
		contextImage = loriImage.bufferedImage

		var newImage = contextImage

		for (i in 0 until 256) {
			// determine scale
			var scaleTo = if (i % 2 == 0) SeamCarver.CarveDirection.HORIZONTAL else SeamCarver.CarveDirection.VERTICAL

			if (32 > newImage.height) { // se ficar menos que 32 irá ficar bem ruim a imagem
				break
			}

			if (32 > newImage.width) { // se ficar menos que 32 irá ficar bem ruim a imagem
				break
			}

			// Get the new image w/o one seam.
			newImage = SeamCarver.carveSeam(newImage, scaleTo)
		}

		context.sendFile(newImage, "content_aware_scale.png", context.getAsMention(true))
	}
}