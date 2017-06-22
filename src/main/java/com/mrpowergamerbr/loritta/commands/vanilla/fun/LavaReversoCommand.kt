package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

class LavaReversoCommand : CommandBase() {
	override fun getLabel(): String {
		return "lavareverso"
	}

	override fun getDescription(): String {
		return "O chão é...? Decida o que você quiser!";
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta servidores brasileiros");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN;
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			var contextImage = LorittaUtils.getImageFromContext(context, 0, 0);
			var template = ImageIO.read(File(Loritta.FOLDER + "lavareverso.png")); // Template

			if (contextImage == null) {
				contextImage = LorittaUtils.getImageFromContext(context, 0);
				if (!LorittaUtils.isValidImage(context, contextImage)) {
					return;
				}
			} else {
				context.rawArgs = context.rawArgs.sliceArray(1..context.rawArgs.size - 1);
			}

			if (context.rawArgs.isEmpty()) {
				this.explain(context);
				return;
			}

			var joined = context.rawArgs.joinToString(separator = " "); // Vamos juntar tudo em uma string
			var singular = true; // E verificar se é singular ou não
			if (context.rawArgs[0].endsWith("s", true)) { // Se termina com s...
				singular = false; // Então é plural!
			}
			// Redimensionar, se nós não fizermos isso, vai ficar bugado na hora de dar rotate
			var firstImage = contextImage.getScaledInstance(256, 256, BufferedImage.SCALE_SMOOTH);
			// E agora aumentar o tamanho da canvas
			var firstImageCanvas = BufferedImage(326, 326, BufferedImage.TYPE_INT_ARGB);
			var firstImageCanvasGraphics = firstImageCanvas.graphics;
			firstImageCanvasGraphics.drawImage(firstImage, 35, 35, null);

			var transform = AffineTransform();
			transform.rotate(0.436332, (firstImageCanvas.getWidth() / 2).toDouble(), (firstImageCanvas.getHeight() / 2).toDouble());
			var op = AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
			var rotated = op.filter(firstImageCanvas, null);

			var resized = rotated.getScaledInstance(196, 196, BufferedImage.SCALE_SMOOTH);
			var small = contextImage.getScaledInstance(111, 111, BufferedImage.SCALE_SMOOTH);
			var templateGraphics = template.graphics;
			templateGraphics.drawImage(resized, 100, 0, null);
			templateGraphics.drawImage(small, 464, 175, null);
			var image = BufferedImage(693, 766, BufferedImage.TYPE_INT_ARGB);
			var graphics = image.getGraphics() as java.awt.Graphics2D;
			graphics.color = Color.WHITE;
			graphics.fillRect(0, 0, 693, 766);
			graphics.color = Color.BLACK;
			graphics.drawImage(template, 0, 100, null);
			graphics.setRenderingHint(
					java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
					java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			var font = Font.createFont(0, File(Loritta.FOLDER + "mavenpro-bold.ttf")).deriveFont(32F);
			graphics.font = font;
			ImageUtils.drawCenteredString(graphics, "O chão " + (if (singular) "é" else "são") + " $joined", Rectangle(2, 2, 693, 100), font);

			val os = ByteArrayOutputStream()
			ImageIO.write(image, "png", os)
			val inputStream = ByteArrayInputStream(os.toByteArray())

			context.sendFile(inputStream, "lavareverso.png", context.getAsMention(true));
		} else {
			this.explain(context);
		}
	}
}