package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.gifs.SwingGIF
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.utils.LorittaUtils
import net.perfectdreams.loritta.legacy.utils.MiscUtils
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.api.commands.Command
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory

class SwingCommand : AbstractCommand("swing", category = CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.swing.description")
	override fun getExamplesKey() = Command.TWO_IMAGES_EXAMPLES_KEY

	// TODO: Fix Usage

	override fun needsToUploadFiles() = true

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		// We want search = 0 because we want to use the user's avatar if they didn't mention anything else
		val contextImage2 = context.getImageAt(1, search = 0) ?: LorittaUtils.downloadImage(context.userHandle.effectiveAvatarUrl) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val file = SwingGIF.getGIF(contextImage, contextImage2)
		MiscUtils.optimizeGIF(file)
		context.sendFile(file, "swing.gif", context.getAsMention(true))
		file.delete()
	}
}