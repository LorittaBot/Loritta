package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.utils.LorittaImage
import net.perfectdreams.loritta.legacy.utils.SeamCarver
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.api.commands.ArgumentType
import net.perfectdreams.loritta.legacy.api.commands.Command
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.api.commands.arguments

class ContentAwareScaleCommand : AbstractCommand("contentawarescale", listOf("cas", "contentaware", "seamcarver"), category = CommandCategory.IMAGES) {
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