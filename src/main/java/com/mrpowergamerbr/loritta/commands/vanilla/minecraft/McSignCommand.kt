package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO

class McSignCommand : CommandBase() {
	override fun getLabel(): String {
		return "mcsign"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.MCSIGN_DESCRIPTION.msgFormat();
	}

	override fun getExample(): List<String> {
		return listOf("Isto é um texto | em uma placa! | | Legal né?");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MINECRAFT;
	}

	override fun getUsage(): String {
		return "texto";
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val lines = context.args.joinToString(" ").split("|");
			lines.forEach { it.trim() }
			val template = ImageIO.read(File(Loritta.FOLDER + "sign.png")) // Template

			val graphics = template.graphics

			val minecraftia = Font.createFont(Font.TRUETYPE_FONT, FileInputStream(File(Loritta.FOLDER + "minecraftia.ttf")))
					.deriveFont(17.toFloat()) // A fonte para colocar na placa

			graphics.font = minecraftia;
			graphics.color = Color(0, 0, 0)
			var currentY = 2;

			for (i in 0..lines.size - 1) {
				ImageUtils.drawCenteredString(graphics, lines[i], Rectangle(0, currentY, 192, 23), minecraftia);
				currentY += 23;
			}

			context.sendFile(template, "placa.png", context.getAsMention(true))
		} else {
			context.explain()
		}
	}
}