package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.gifs.CepoDeMadeiraGIF
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.MiscUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.common.commands.CommandCategory

class CepoCommand : AbstractCommand("cepo", category = CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.cepo.description")
	override fun getExamplesKey() = Command.SINGLE_IMAGE_EXAMPLES_KEY
	// TODO: Fix Usage

	override fun needsToUploadFiles() = true

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val file = CepoDeMadeiraGIF.getGIF(contextImage)

		MiscUtils.optimizeGIF(file)
		context.sendFile(file, "cepo.gif", context.getAsMention(true))
		file.delete()
	}
}