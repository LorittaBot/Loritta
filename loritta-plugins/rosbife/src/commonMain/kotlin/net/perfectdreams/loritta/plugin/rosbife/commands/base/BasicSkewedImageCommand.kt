package net.perfectdreams.loritta.plugin.rosbife.commands.base

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandBuilder
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.utils.createImage
import net.perfectdreams.loritta.api.utils.image.Image

interface BasicSkewedImageCommand : BasicImageCommand {
	val corners: List<Corners>

	override fun create(loritta: LorittaBot, labels: List<String>, builder: CommandBuilder<CommandContext>.() -> (Unit)): Command<CommandContext> {
		return super.create(
				loritta,
				labels
		) {
			executes {
				val contextImage = validate(image(0))
				val template = loritta.assets.loadImage(sourceTemplatePath, loadFromCache = true)

				val base = createImage(template.width, template.height)
				val graphics = base.createGraphics()

				corners.forEach {
					// Nós iremos clonar a imagem para que ela fique em formato ARGB, isso evita problemas de transparência
					// val clonedContextImage = BufferedImage(contextImage.width, contextImage.height, BufferedImage.TYPE_INT_ARGB)
					// clonedContextImage.graphics.drawImage(contextImage, 0, 0, null)

					// skew image
					val skewed = contextImage.getScaledInstance(template.width, template.height, Image.ScaleType.SMOOTH).getSkewedInstance(
							it.upperLeftX, it.upperLeftY,
							it.upperRightX, it.upperRightY,
							it.lowerRightX, it.lowerRightY,
							it.lowerLeftX, it.lowerLeftY
					)

					graphics.drawImage(skewed, 0, 0)
				}

				graphics.drawImage(template, 0, 0)

				sendImage(base, sourceTemplatePath)
			}
		}
	}

	data class Corners(
			val upperLeftX: Float, val upperLeftY: Float,
			val upperRightX: Float, val upperRightY: Float,
			val lowerRightX: Float, val lowerRightY: Float,
			val lowerLeftX: Float, val lowerLeftY: Float
	)
}