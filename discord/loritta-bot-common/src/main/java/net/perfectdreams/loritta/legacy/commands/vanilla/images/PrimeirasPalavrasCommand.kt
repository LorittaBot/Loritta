package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.Loritta
import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.ImageUtils
import net.perfectdreams.loritta.legacy.utils.enableFontAntiAliasing
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.utils.extensions.readImage
import java.awt.Color
import java.awt.Font
import java.io.File

class PrimeirasPalavrasCommand : AbstractCommand("firstwords", listOf("primeiraspalavras"), CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.firstwords.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.firstwords.examples")

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