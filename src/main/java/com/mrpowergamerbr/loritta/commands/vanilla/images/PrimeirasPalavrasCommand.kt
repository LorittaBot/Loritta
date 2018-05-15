package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.io.File
import javax.imageio.ImageIO

class PrimeirasPalavrasCommand : AbstractCommand("firstwords", listOf("primeiraspalavras"), CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["PRIMEIRAS_DESCRIPTION"]
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val str = context.args.joinToString(" ")

			val bi = ImageIO.read(File(Loritta.ASSETS + "tirinha_baby.png")) // Primeiro iremos carregar o nosso template

			val baseGraph = bi.graphics

			(baseGraph as Graphics2D).setRenderingHint(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

			baseGraph.setColor(Color(0, 0, 0, 255))

			val font = Font("Arial", Font.BOLD, 32)

			baseGraph.setFont(font)

			val quaseFalando = str[0] + "... " + str[0] + "..."

			ImageUtils.drawTextWrap(quaseFalando, 4, 5 + font.size, 236, 0, baseGraph.getFontMetrics(), baseGraph)

			ImageUtils.drawTextWrapSpaces(str, 4, 277 + font.size, 342, 0, baseGraph.getFontMetrics(), baseGraph)

			context.sendFile(bi, "tirinha_baby.png", context.getAsMention(true))
		} else {
			this.explain(context)
		}
	}
}