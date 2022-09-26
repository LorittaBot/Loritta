package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.Loritta
import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.api.commands.Command
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class PerfeitoCommand : AbstractCommand("perfect", listOf("perfeito"), CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.perfect.description")
	override fun getExamplesKey() = Command.SINGLE_IMAGE_EXAMPLES_KEY

	// TODO: Fix Usage

	override fun needsToUploadFiles() = true

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val contextImage = context.getImageAt(0, avatarSize = 256) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val template = ImageIO.read(File(Loritta.ASSETS + "perfeito.png")) // Template

		val scaled = contextImage.getScaledInstance(231, 231, BufferedImage.SCALE_SMOOTH)
		template.graphics.drawImage(scaled, 225, 85, null)

		context.sendFile(template, "perfeito.png", context.getAsMention(true))
	}
}