package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.Loritta
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.api.commands.Command
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import java.awt.image.BufferedImage
import java.io.File

class RipVidaCommand : AbstractCommand("riplife", listOf("ripvida"), net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.ripvida.description")
	override fun getExamplesKey() = Command.SINGLE_IMAGE_EXAMPLES_KEY

	// TODO: Fix Usage

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val template = readImage(File(Loritta.ASSETS + context.locale["commands.command.ripvida.file"])) // Template

		val scaled = contextImage.getScaledInstance(133, 133, BufferedImage.SCALE_SMOOTH)
		template.graphics.drawImage(scaled, 133, 0, null)

		context.sendFile(template, context.locale["commands.command.ripvida.file"], context.getAsMention(true))
	}
}