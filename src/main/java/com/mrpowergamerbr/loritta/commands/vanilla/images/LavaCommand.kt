package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class LavaCommand : AbstractCommand("lava", category = CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["LAVA_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta bots indecentes");
	}

	override fun getUsage(): String {
		return "<imagem>";
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			var contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
			val template = ImageIO.read(File(Loritta.ASSETS + "lava.png")); // Template

			context.rawArgs = context.rawArgs.sliceArray(1..context.rawArgs.size - 1);

			if (context.rawArgs.isEmpty()) {
				this.explain(context)
				return
			}

			var joined = context.rawArgs.joinToString(separator = " "); // Vamos juntar tudo em uma string
			var singular = true; // E verificar se é singular ou não
			if (context.rawArgs[0].endsWith("s", true)) { // Se termina com s...
				singular = false; // Então é plural!
			}
			var resized = contextImage.getScaledInstance(64, 64, BufferedImage.SCALE_SMOOTH);
			var small = contextImage.getScaledInstance(32, 32, BufferedImage.SCALE_SMOOTH);
			var templateGraphics = template.graphics;
			templateGraphics.drawImage(resized, 120, 0, null);
			templateGraphics.drawImage(small, 487, 0, null);
			var image = BufferedImage(700, 443, BufferedImage.TYPE_INT_ARGB);
			var graphics = image.getGraphics() as java.awt.Graphics2D;
			graphics.color = Color.WHITE;
			graphics.fillRect(0, 0, 700, 443);
			graphics.color = Color.BLACK;
			graphics.drawImage(template, 0, 100, null);
			graphics.setRenderingHint(
					java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
					java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			var font = Font.createFont(0, File(Loritta.ASSETS + "mavenpro-bold.ttf")).deriveFont(24F);
			graphics.font = font;
			ImageUtils.drawCenteredString(graphics, "O chão " + (if (singular) "é" else "são") + " $joined", Rectangle(2, 2, 700, 100), font);

			context.sendFile(image, "lava.png", context.getAsMention(true));
		} else {
			this.explain(context);
		}
	}
}