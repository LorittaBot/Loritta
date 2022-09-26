package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.gifs.DemonGIF
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.MiscUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.api.commands.Command
import net.perfectdreams.loritta.morenitta.LorittaBot

class DemonCommand(loritta: LorittaBot) : AbstractCommand(loritta, "demon", listOf("demônio", "demonio", "demónio"), category = net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES) {
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