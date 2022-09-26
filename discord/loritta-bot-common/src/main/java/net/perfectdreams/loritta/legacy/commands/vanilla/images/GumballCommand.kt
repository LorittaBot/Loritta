package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.gifs.GumballGIF
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.utils.MiscUtils
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.api.commands.Command
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory

class GumballCommand : AbstractCommand("gumball", category = CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.gumballliftup.description")
	override fun getExamplesKey() = Command.SINGLE_IMAGE_EXAMPLES_KEY

	// TODO: Fix Usage

	override fun needsToUploadFiles() = true

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val file = GumballGIF.getGIF(contextImage, locale)
		MiscUtils.optimizeGIF(file)
		context.sendFile(file, "gumball.gif", context.getAsMention(true))
		file.delete()
	}
}