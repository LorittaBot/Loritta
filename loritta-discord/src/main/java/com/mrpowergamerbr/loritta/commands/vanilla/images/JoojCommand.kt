package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.google.common.collect.ImmutableMap
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.util.*

class JoojCommand : AbstractCommand("jooj", category = CommandCategory.IMAGES) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.get("JOOJ_DESCRIPTION")
	}

	override fun getExamples(): List<String> {
		return Arrays.asList("@Loritta")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return ImmutableMap.builder<String, String>()
				.put("imagem", "imagem")
				.build()
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val image = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val leftSide = image.getSubimage(0, 0, image.width / 2, image.height)

		// Girar a imagem horizontalmente
		val tx = AffineTransform.getScaleInstance(-1.0, 1.0)
		tx.translate(-leftSide.getWidth(null).toDouble(), 0.0)
		val op = AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
		val leftSideFlipped = op.filter(leftSide, null)

		image.graphics.drawImage(leftSideFlipped, image.width / 2, 0, null)

		context.sendFile(image, "jooj.png", context.getAsMention(true))
	}
}