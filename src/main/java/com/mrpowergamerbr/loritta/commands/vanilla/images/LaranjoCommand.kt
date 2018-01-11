package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.Color
import java.awt.Font
import java.io.File
import javax.imageio.ImageIO

class LaranjoCommand : AbstractCommand("laranjo", category = CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["LARANJO_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return listOf("ei ademin bane o cara ai pfv");
	}

	override fun getUsage(): String {
		return "<texto>";
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val template = ImageIO.read(File(Loritta.ASSETS + "laranjo.png")); // Template
			val texto = context.args.joinToString(" ");

			var graphics = template.graphics as java.awt.Graphics2D;
			graphics.color = Color.BLACK;
			graphics.setRenderingHint(
					java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
					java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			var font = Font.createFont(0, File(Loritta.ASSETS + "mavenpro-bold.ttf")).deriveFont(24F);
			graphics.font = font;
			ImageUtils.drawTextWrapSpaces(texto, 2, 40, 334, 9999, graphics.fontMetrics, graphics);

			context.sendFile(template, "laranjo.png", context.getAsMention(true));
		} else {
			context.explain()
		}
	}
}