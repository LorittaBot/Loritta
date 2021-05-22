package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.enableFontAntiAliasing
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File

class LavaReversoCommand : AbstractCommand("lavareverse", listOf("lavareverso", "reverselava"), CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.reverselava.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.reverselava.examples")

	override fun getUsage() = arguments {
		argument(ArgumentType.IMAGE) {}
		argument(ArgumentType.TEXT) {}
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
			val template = readImage(File(Loritta.ASSETS + "lavareverso.png")) // Template

			context.rawArgs = context.rawArgs.sliceArray(1..context.rawArgs.size - 1)

			if (context.rawArgs.isEmpty()) {
				this.explain(context)
				return
			}

			var joined = context.rawArgs.joinToString(separator = " ") // Vamos juntar tudo em uma string
			var singular = true // E verificar se é singular ou não
			if (context.rawArgs[0].endsWith("s", true)) { // Se termina com s...
				singular = false // Então é plural!
			}
			// Redimensionar, se nós não fizermos isso, vai ficar bugado na hora de dar rotate
			var firstImage = contextImage.getScaledInstance(256, 256, BufferedImage.SCALE_SMOOTH)
			// E agora aumentar o tamanho da canvas
			var firstImageCanvas = BufferedImage(326, 326, BufferedImage.TYPE_INT_ARGB)
			var firstImageCanvasGraphics = firstImageCanvas.graphics
			firstImageCanvasGraphics.drawImage(firstImage, 35, 35, null)

			var transform = AffineTransform()
			transform.rotate(0.436332, (firstImageCanvas.width / 2).toDouble(), (firstImageCanvas.height / 2).toDouble())
			var op = AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR)
			var rotated = op.filter(firstImageCanvas, null)

			var resized = rotated.getScaledInstance(196, 196, BufferedImage.SCALE_SMOOTH)
			var small = contextImage.getScaledInstance(111, 111, BufferedImage.SCALE_SMOOTH)
			var templateGraphics = template.graphics
			templateGraphics.drawImage(resized, 100, 0, null)
			templateGraphics.drawImage(small, 464, 175, null)
			var image = BufferedImage(693, 766, BufferedImage.TYPE_INT_ARGB)
			var graphics = image.graphics.enableFontAntiAliasing()
			graphics.color = Color.WHITE
			graphics.fillRect(0, 0, 693, 766)
			graphics.color = Color.BLACK
			graphics.drawImage(template, 0, 100, null)

			var font = Font.createFont(0, File(Loritta.ASSETS + "mavenpro-bold.ttf")).deriveFont(32F)
			graphics.font = font
			ImageUtils.drawCenteredString(graphics, "O chão " + (if (singular) "é" else "são") + " $joined", Rectangle(2, 2, 693, 100), font)

			context.sendFile(image, "lavareverso.png", context.getAsMention(true))
		} else {
			this.explain(context)
		}
	}
}