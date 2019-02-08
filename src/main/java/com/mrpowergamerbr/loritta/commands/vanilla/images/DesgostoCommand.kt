package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class DesgostoCommand : AbstractCommand("desgosto", category = CommandCategory.IMAGES) {
	companion object {
		val TEMPLATE by lazy { ImageIO.read(File(Loritta.ASSETS + "desgosto/desgosto.png")) }
		val OVERLAY by lazy { ImageIO.read(File(Loritta.ASSETS + "degosto/overlay.png")) }
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["DESGOSTO_DESCRIPTION"]
	}

	override fun getExamples(): List<String> {
		return Arrays.asList("@Pantufa")
	}

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.USER) {}
			argument(ArgumentType.IMAGE) {}
		}
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val base = BufferedImage(412, 528, BufferedImage.TYPE_INT_ARGB)

		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val scaled = contextImage.getScaledInstance(412, 371, BufferedImage.SCALE_SMOOTH).toBufferedImage()

		base.graphics.drawImage(TEMPLATE, 0, 0, null)
		base.graphics.drawImage(scaled, 0, 0, null)
		base.graphics.drawImage(OVERLAY, 0, 0, null)

		context.sendFile(base, "desgosto.png", context.getAsMention(true))
	}
}
