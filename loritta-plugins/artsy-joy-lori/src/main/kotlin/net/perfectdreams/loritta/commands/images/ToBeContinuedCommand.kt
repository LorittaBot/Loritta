package net.perfectdreams.loritta.commands.images

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommandContext
import net.perfectdreams.loritta.api.commands.notNullImage
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.contracts.ExperimentalContracts

open class ToBeContinuedCommand : BasicImageCommand(
		arrayOf("tobecontinued"),
		CommandCategory.IMAGES,
		"commands.images.tobecontinued.description",
		"to_be_continued_arrow.png"
) {
	@ExperimentalContracts
	@Subcommand
	suspend fun run(context: LorittaCommandContext, locale: BaseLocale) {
		val contextImage = notNullImage(context.getImageAt(0), context)

		val sepiaEffectImage = BufferedImage(contextImage.width, contextImage.height, BufferedImage.TYPE_INT_ARGB)
		val sepiaEffectImageGraphics = sepiaEffectImage.graphics

		for (x in 0 until contextImage.width) {
			for (y in 0 until contextImage.height) {
				val rgb = contextImage.getRGB(x, y)

				val color = Color(rgb)

				// https://stackoverflow.com/questions/1061093/how-is-a-sepia-tone-created
				val sepiaRed = (color.red * .393) + (color.green *.769) + (color.blue * .189)
				val sepiaGreen = (color.red * .349) + (color.green *.686) + (color.blue * .168)
				val sepiaBlue = (color.red * .272) + (color.green *.534) + (color.blue * .131)

				sepiaEffectImage.setRGB(
						x, y,
						Color(
								Math.min(
										255,
										sepiaRed.toInt()
								),
								Math.min(
										255,
										sepiaGreen.toInt()
								),
								Math.min(
										255,
										sepiaBlue.toInt()
								)
						).rgb
				)
			}
		}

		// Em 1280x720
		// A margem entre a parte esquerda até a seta é 42 pixels
		// A margem entre a parte de baixo até a seta também é 42 pixels
		// A largura da seta é 664
		// Altura é 152
		val padding = (42 * sepiaEffectImage.width) / 1280

		val arrowWidth = (664 * sepiaEffectImage.width) / 1280
		val arrowHeight = (152 * arrowWidth) / 664

		sepiaEffectImageGraphics.drawImage(
				template
						.getScaledInstance(
								arrowWidth,
								arrowHeight,
								BufferedImage.SCALE_SMOOTH
						),
				padding,
				sepiaEffectImage.height - padding - arrowHeight,
				null
		)

		context.sendFile(sepiaEffectImage, "to_be_continued.png", context.getAsMention(true))
	}
}