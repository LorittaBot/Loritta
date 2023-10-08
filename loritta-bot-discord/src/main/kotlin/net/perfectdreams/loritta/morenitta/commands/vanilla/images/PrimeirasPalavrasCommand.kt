package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.ImageUtils
import net.perfectdreams.loritta.morenitta.utils.enableFontAntiAliasing
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import java.awt.Color
import java.awt.Font
import java.io.File

class PrimeirasPalavrasCommand(loritta: LorittaBot) : AbstractCommand(loritta, "firstwords", listOf("primeiraspalavras"), net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.firstwords.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.firstwords.examples")

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val str = context.args.joinToString(" ")

			val bi = readImage(File(LorittaBot.ASSETS + "tirinha_baby.png")) // Primeiro iremos carregar o nosso template

			val baseGraph = bi.graphics.enableFontAntiAliasing()

            baseGraph.color = Color(0, 0, 0, 255)

			val font = Font("Arial", Font.BOLD, 32)

            baseGraph.font = font

			val quaseFalando = str[0] + "... " + str[0] + "..."

			ImageUtils.drawTextWrap(loritta, quaseFalando, 4, 5 + font.size, 236, 0, baseGraph.fontMetrics, baseGraph)

			ImageUtils.drawTextWrapSpaces(loritta, str, 4, 277 + font.size, 342, 0, baseGraph.fontMetrics, baseGraph)

			context.sendFile(bi, "tirinha_baby.png", context.getAsMention(true))
		} else {
			this.explain(context)
		}
	}
}