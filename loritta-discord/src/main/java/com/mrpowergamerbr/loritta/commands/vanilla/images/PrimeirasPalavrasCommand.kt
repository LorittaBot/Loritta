package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.enableFontAntiAliasing
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.Color
import java.awt.Font
import java.io.File
import javax.imageio.ImageIO

class PrimeirasPalavrasCommand : AbstractCommand("firstwords", listOf("primeiraspalavras"), CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["commands.images.firstwords.description"]
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val str = context.args.joinToString(" ")

			val bi = readImage(File(Loritta.ASSETS + "tirinha_baby.png")) // Primeiro iremos carregar o nosso template

			val baseGraph = bi.graphics.enableFontAntiAliasing()

            baseGraph.color = Color(0, 0, 0, 255)

			val font = Font("Arial", Font.BOLD, 32)

            baseGraph.font = font

			val quaseFalando = str[0] + "... " + str[0] + "..."

			ImageUtils.drawTextWrap(quaseFalando, 4, 5 + font.size, 236, 0, baseGraph.fontMetrics, baseGraph)

			ImageUtils.drawTextWrapSpaces(str, 4, 277 + font.size, 342, 0, baseGraph.fontMetrics, baseGraph)

			context.sendFile(bi, "tirinha_baby.png", context.getAsMention(true))
		} else {
			this.explain(context)
		}
	}
}