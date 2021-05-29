package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.gifs.DemonGIF
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.MiscUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.common.commands.CommandCategory

class DemonCommand : AbstractCommand("demon", listOf("demônio", "demonio", "demónio"), category = CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.demon.description")
	override fun getExamplesKey() = Command.TWO_IMAGES_EXAMPLES_KEY

	// TODO: Fix Usage

	override fun needsToUploadFiles() = true

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val file = DemonGIF.getGIF(contextImage, context.config.localeId)

		MiscUtils.optimizeGIF(file)
		context.sendFile(file, "demon.gif", context.getAsMention(true))
		file.delete()
	}
}