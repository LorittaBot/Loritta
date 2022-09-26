package net.perfectdreams.loritta.legacy.commands.vanilla.images

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

class PerdaoCommand : AbstractCommand("perdao", listOf("perdão"), CommandCategory.IMAGES) {
	companion object {
		val TEMPLATE by lazy { ImageIO.read(File(Constants.ASSETS_FOLDER, "perdao.png")) }
	}

	override fun getDescriptionKey() = LocaleKeyData("commands.command.forgive.description")
	override fun getExamplesKey() = Command.SINGLE_IMAGE_EXAMPLES_KEY

	// TODO: Fix Usage

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		// RULE OF THREE!!11!
		// larguraOriginal - larguraDoContextImage
		// alturaOriginal - X
		val newHeight = (contextImage.width * TEMPLATE.height) / TEMPLATE.width

		val scaledTemplate = TEMPLATE.getScaledInstance(contextImage.width, Math.max(newHeight, 1), BufferedImage.SCALE_SMOOTH)
		contextImage.graphics.drawImage(scaledTemplate, 0, contextImage.height - scaledTemplate.getHeight(null), null)

		context.sendFile(contextImage, "perdao.png", context.getAsMention(true))
	}
}