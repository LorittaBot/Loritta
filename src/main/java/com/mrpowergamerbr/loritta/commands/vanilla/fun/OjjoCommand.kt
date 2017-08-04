package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.google.common.collect.ImmutableMap
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.util.*

class OjjoCommand : CommandBase() {
	override fun getLabel(): String {
		return "ojjo"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.get("OJJO_COMMAND")
	}

	override fun getExample(): List<String> {
		return Arrays.asList("@Loritta")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return ImmutableMap.builder<String, String>()
				.put("imagem", "imagem")
				.build()
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
		val image = LorittaUtils.getImageFromContext(context, 0)

		if (!LorittaUtils.isValidImage(context, image)) { return }

		val rightSide = image.getSubimage(image.width / 2, 0, image.width, image.height)

		// Girar a imagem horizontalmente
		val tx = AffineTransform.getScaleInstance(-1.0, 1.0);
		tx.translate(-rightSide.getWidth(null).toDouble(), 0.0);
		val op = AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		val rightSideFlipped = op.filter(image, null);

		image.graphics.drawImage(rightSideFlipped, 0, 0, null)

		context.sendFile(image, "ojjo.png", context.getAsMention(true))
	}
}