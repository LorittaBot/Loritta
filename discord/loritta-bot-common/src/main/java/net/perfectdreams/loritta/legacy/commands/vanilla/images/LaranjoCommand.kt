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

class LaranjoCommand : AbstractCommand("laranjo", category = CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.laranjo.description")

	override fun getExamples(): List<String> {
		return listOf("ei ademin bane o cara ai pfv")
	}

	// TODO: Fix Usage

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val template = readImage(File(Loritta.ASSETS + "laranjo.png")) // Template
			val texto = context.args.joinToString(" ")

			var graphics = template.graphics.enableFontAntiAliasing()
			graphics.color = Color.BLACK

			var font = Font.createFont(0, File(Loritta.ASSETS + "mavenpro-bold.ttf")).deriveFont(24F)
			graphics.font = font
			ImageUtils.drawTextWrapSpaces(texto, 2, 40, 334, 9999, graphics.fontMetrics, graphics)

			context.sendFile(template, "laranjo.png", context.getAsMention(true))
		} else {
			context.explain()
		}
	}
}