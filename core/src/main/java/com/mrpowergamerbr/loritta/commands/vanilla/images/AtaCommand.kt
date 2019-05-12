package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaImage
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class AtaCommand : AbstractCommand("ata", category = CommandCategory.IMAGES) {
	companion object {
		val TEMPLATE by lazy { ImageIO.read(File(Constants.ASSETS_FOLDER, "ata.png")) }
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.get("ATA_DESCRIPTION")
	}

	override fun getExamples(): List<String> {
		return listOf("@Loritta")
	}

	override fun getUsage(): String {
		return "<imagem>"
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val base = BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB)
		val scaled = contextImage.getScaledInstance(300, 300, BufferedImage.SCALE_SMOOTH).toBufferedImage()

		val transformed = LorittaImage(scaled)
		transformed.setCorners(107F, 0F,
				300F, 0F,
				300F, 177F,
				96F, 138F)

		base.graphics.drawImage(transformed.bufferedImage, 0, 0, null)
		base.graphics.drawImage(TEMPLATE, 0, 0, null)

		context.sendFile(base, "ata.png", context.getAsMention(true))
	}
}