package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.gifs.GifSequenceWriter
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageOutputStream

class TriggeredCommand : AbstractCommand("triggered") {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("RIPVIDA_DESCRIPTION")
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.IMAGES
	}

	override fun getUsage(): String {
		return "<imagem>";
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var contextImage = LorittaUtils.getImageFromContext(context, 0)
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return
		}

		val input = contextImage

		val triggeredLabel = ImageIO.read(File(Loritta.ASSETS, "triggered.png"))
		// scale

		var subtractW = input.width / 16
		var subtractH = input.height / 16
		var inputWidth = input.width - subtractW
		var inputHeight = input.height - subtractH

		// ogWidth --- input.width
		// ogHeight --- x
		var a1 = triggeredLabel.height * inputWidth
		var labelHeight = a1 / triggeredLabel.width

		val scaledTriggeredLabel = triggeredLabel.getScaledInstance(inputWidth, labelHeight, BufferedImage.SCALE_SMOOTH)

		val base = BufferedImage(inputWidth, inputHeight + scaledTriggeredLabel.getHeight(null), BufferedImage.TYPE_INT_ARGB)
		val tint = BufferedImage(base.width, inputHeight, BufferedImage.TYPE_INT_ARGB)

		val color = Color(255, 0, 0, 60)
		val graphics = base.graphics
		val tintGraphics = tint.graphics
		tintGraphics.color = color
		tintGraphics.fillRect(0, 0, tint.width, tint.height)

		var fileName = Loritta.TEMP + "triggered-" + System.currentTimeMillis() + ".gif"
		val outputFile = File(fileName)
		var output = FileImageOutputStream(outputFile)

		val writer = GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, 4, true)

		for (i in 0..5) {
			var offsetX = Loritta.RANDOM.nextInt(0, subtractW)
			var offsetY = Loritta.RANDOM.nextInt(0, subtractH)

			val subimage = input.getSubimage(offsetX, offsetY, inputWidth, inputHeight)

			graphics.drawImage(subimage, 0, 0, null)

			graphics.drawImage(tint, 0, 0, null)
			graphics.drawImage(scaledTriggeredLabel, 0, inputHeight, null)
			writer.writeToSequence(base)
		}

		writer.close()
		output.close()

		context.sendFile(outputFile, "triggered.gif", context.getAsMention(true))
		outputFile.delete()
	}
}