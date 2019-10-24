package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.enableFontAntiAliasing
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import java.awt.Color
import java.awt.Font
import java.io.File
import javax.imageio.ImageIO

class LaranjoCommand : AbstractCommand("laranjo", category = CommandCategory.IMAGES) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["LARANJO_DESCRIPTION"]
	}

	override fun getExamples(): List<String> {
		return listOf("ei ademin bane o cara ai pfv")
	}

	override fun getUsage(): String {
		return "<texto>"
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val template = ImageIO.read(File(Loritta.ASSETS + "laranjo.png")) // Template
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