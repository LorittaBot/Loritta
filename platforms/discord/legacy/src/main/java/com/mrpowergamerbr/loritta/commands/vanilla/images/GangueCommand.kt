package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaImage
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.makeRoundedCorners
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class GangueCommand : AbstractCommand("gang", listOf("gangue"), CommandCategory.IMAGES) {
	companion object {
		val TEMPLATE_OVERLAY by lazy { ImageIO.read(File(Constants.ASSETS_FOLDER, "cocielo/overlay.png")) }
	}

	override fun getDescriptionKey() = LocaleKeyData("commands.command.gang.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.gang.examples")

	// TODO: Fix Usage

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val contextImage2 = context.getImageAt(1) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val contextImage3 = context.getImageAt(2) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val contextImage4 = context.getImageAt(3) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val contextImage5 = context.getImageAt(4) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val template = readImage(File(Loritta.ASSETS + "cocielo/cocielo.png"))

		val scaled = contextImage.getScaledInstance(59, 59, BufferedImage.SCALE_SMOOTH)
				.toBufferedImage()
				.makeRoundedCorners(20)
		val scaled2 = contextImage2.getScaledInstance(47, 57, BufferedImage.SCALE_SMOOTH)
				.toBufferedImage()
				.makeRoundedCorners(20)
		val scaled3 = contextImage3.getScaledInstance(50, 50, BufferedImage.SCALE_SMOOTH)
				.toBufferedImage()
				.makeRoundedCorners(20)
		val scaled4 = contextImage4.getScaledInstance(53, 58, BufferedImage.SCALE_SMOOTH)
				.toBufferedImage()
				.makeRoundedCorners(20)
		val scaled5 = contextImage5.getScaledInstance(43, 43, BufferedImage.SCALE_SMOOTH)
				.toBufferedImage()
				.makeRoundedCorners(20)

		// Porque nós precisamos rotacionar
		val rotated = LorittaImage(scaled5)
		rotated.rotate(335.0)

		template.graphics.drawImage(scaled, 216, 80, null)
		template.graphics.drawImage(scaled2, 142, 87, null)
		template.graphics.drawImage(scaled3, 345, 80, null)
		template.graphics.drawImage(scaled4, 28, 141, null)
		template.graphics.drawImage(rotated.bufferedImage, 290, -5, null)
		template.graphics.drawImage(TEMPLATE_OVERLAY, 0, 0, null)
		context.sendFile(template, "gangue.png", context.getAsMention(true))
	}
}