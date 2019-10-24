package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.enableFontAntiAliasing
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class DeusesCommand : AbstractCommand("deuses", category = CommandCategory.IMAGES) {
	companion object {
		val TEMPLATE by lazy { ImageIO.read(File(Constants.ASSETS_FOLDER, "deuses.png")) }
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["DEUSES_DESCRIPTION"]
	}

	override fun getExamples(): List<String> {
		return listOf("Quando você é nível 4 e vê pessoas de nível 100 jogando")
	}

	override fun getUsage(): String {
		return "<texto>"
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val texto = context.args.joinToString(" ")

			// Vamos criar o nosso tempalte
			val image = BufferedImage(630, 830, BufferedImage.TYPE_INT_ARGB)
			val graphics = image.graphics.enableFontAntiAliasing()
			graphics.color = Color.WHITE
			graphics.fillRect(0, 0, 630, 830)
			graphics.color = Color.BLACK
			graphics.drawImage(TEMPLATE, 0, 200, null)

			val font = Font.createFont(0, File(Loritta.ASSETS + "mavenpro-bold.ttf")).deriveFont(42F)
			graphics.font = font
			ImageUtils.drawTextWrapSpaces(texto, 2, 40, 630, 9999, graphics.fontMetrics, graphics)

			context.sendFile(image, "deuses.png", context.getAsMention(true))
		} else {
			context.explain()
		}
	}
}