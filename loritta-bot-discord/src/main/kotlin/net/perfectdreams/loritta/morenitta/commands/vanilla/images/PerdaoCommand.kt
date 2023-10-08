package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.api.commands.Command
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import net.perfectdreams.loritta.morenitta.LorittaBot

class PerdaoCommand(loritta: LorittaBot) : AbstractCommand(loritta, "perdao", listOf("perdão"), net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES) {
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