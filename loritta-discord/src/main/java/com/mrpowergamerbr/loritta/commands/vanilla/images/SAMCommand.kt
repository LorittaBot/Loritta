package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import java.awt.Image
import java.io.File
import javax.imageio.ImageIO

class SAMCommand : AbstractCommand("sam", listOf("southamericamemes"), CommandCategory.IMAGES) {
	companion object {
		val SAM_LOGO by lazy { ImageIO.read(File(Constants.ASSETS_FOLDER, "selo_sam.png")) }
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["SAM_DESCRIPTION"]
	}

	override fun getExamples(): List<String> {
		return listOf("https://cdn.discordapp.com/attachments/265632341530116097/297440837871206420/meme.png")
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		var div: Double? = 1.5

		if (context.args.size >= 2) {
			div = context.args[1].toDoubleOrNull()
		}

		if (div == null) {
			div = 1.5
		}

		val image = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val height = (image.height / div).toInt() // Baseando na altura
		val seloSouthAmericaMemes = SAM_LOGO.getScaledInstance(height, height, Image.SCALE_SMOOTH)

		val x = Loritta.RANDOM.nextInt(0, Math.max(1, image.width - seloSouthAmericaMemes.getWidth(null)))
		val y = Loritta.RANDOM.nextInt(0, Math.max(1, image.height - seloSouthAmericaMemes.getHeight(null)))

		image.graphics.drawImage(seloSouthAmericaMemes, x, y, null)

		context.sendFile(image, "south_america_memes.png", context.getAsMention(true))
	}
}