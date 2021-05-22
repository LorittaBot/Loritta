package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.common.commands.CommandCategory
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage

class JoojCommand : AbstractCommand("jooj", category = CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.jooj.description")
	override fun getExamplesKey() = Command.SINGLE_IMAGE_EXAMPLES_KEY

	// TODO: Fix Detailed Usage

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val image = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		// We need to create a empty "base" to avoid issues with transparent images
		val baseImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)

		val leftSide = image.getSubimage(0, 0, image.width / 2, image.height)

		// Girar a imagem horizontalmente
		val tx = AffineTransform.getScaleInstance(-1.0, 1.0)
		tx.translate(-leftSide.getWidth(null).toDouble(), 0.0)
		val op = AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
		val leftSideFlipped = op.filter(leftSide, null)

		baseImage.graphics.drawImage(leftSide, 0, 0, null)
		baseImage.graphics.drawImage(leftSideFlipped, baseImage.width / 2, 0, null)

		context.sendFile(baseImage, "jooj.png", context.getAsMention(true))
	}
}