package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.gifs.SwingGIF
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LocaleKeyData
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandCategory

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