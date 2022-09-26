package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.common.api.commands.ArgumentType
import net.perfectdreams.loritta.common.api.commands.Command
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.ImageAbstractCommandBase
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils
import java.awt.Color
import java.awt.image.BufferedImage

class ToBeContinuedCommand(m: LorittaDiscord) : ImageAbstractCommandBase(m, listOf("tobecontinued")) {
	override fun command() = create {
		localizedDescription("commands.command.tobecontinued.description")
		localizedExamples(Command.SINGLE_IMAGE_EXAMPLES_KEY)

		usage {
			argument(ArgumentType.TEXT) {}
		}

		needsToUploadFiles = true

		executes {
			OutdatedCommandUtils.sendOutdatedCommandMessage(this, locale, "tobecontinued")

			// TODO: Multiplatform
			val mppImage = validate(image(0))
			mppImage as JVMImage
			val mppImageArrow = loritta.assets.loadImage("to_be_continued_arrow.png", loadFromCache = true)
			val template = (mppImageArrow as JVMImage).handle as BufferedImage

			val contextImage = mppImage.handle as BufferedImage

			val sepiaEffectImage = BufferedImage(contextImage.width, contextImage.height, BufferedImage.TYPE_INT_ARGB)
			val sepiaEffectImageGraphics = sepiaEffectImage.createGraphics()

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

			sendImage(JVMImage(sepiaEffectImage), "to_be_continued.png")
		}
	}
}