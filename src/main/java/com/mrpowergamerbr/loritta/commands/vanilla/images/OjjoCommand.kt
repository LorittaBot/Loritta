package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.google.common.collect.ImmutableMap
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.util.*

class OjjoCommand : AbstractCommand("ojjo", category = CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("OJJO_DESCRIPTION")
	}

	override fun getExample(): List<String> {
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

	override fun run(context: CommandContext, locale: BaseLocale) {
		val image = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val rightSide = image.getSubimage(image.width / 2, 0, image.width / 2, image.height)

		// Girar a imagem horizontalmente
		val tx = AffineTransform.getScaleInstance(-1.0, 1.0);
		tx.translate(-rightSide.getWidth(null).toDouble(), 0.0);
		val op = AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		val rightSideFlipped = op.filter(rightSide, null);

		image.graphics.drawImage(rightSideFlipped, 0, 0, null)

		context.sendFile(image, "ojjo.png", context.getAsMention(true))
	}
}