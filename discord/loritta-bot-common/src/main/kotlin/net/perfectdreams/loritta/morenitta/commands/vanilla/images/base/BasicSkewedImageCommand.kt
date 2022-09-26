package net.perfectdreams.loritta.morenitta.commands.vanilla.images.base

import net.perfectdreams.loritta.morenitta.api.commands.CommandBuilder
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.common.utils.createImage
import net.perfectdreams.loritta.common.utils.image.Image
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils

open class BasicSkewedImageCommand(
	loritta: LorittaBot,
	labels: List<String>,
	descriptionKey: String,
	sourceTemplatePath: String,
	val corners: List<Corners>,
	builder: CommandBuilder<CommandContext>.() -> (Unit) = {},
	slashCommandName: String? = null
) : BasicImageCommand(
	loritta,
	labels,
	descriptionKey,
	sourceTemplatePath,
	{
		builder.invoke(this)

		executes {
			slashCommandName?.let {
				OutdatedCommandUtils.sendOutdatedCommandMessage(
					this,
					locale,
					it
				)
			}

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
	},
	slashCommandName
) {
	constructor(
		loritta: LorittaBot,
		labels: List<String>,
		descriptionKey: String,
		sourceTemplatePath: String,
		corner: Corners,
		builder: CommandBuilder<CommandContext>.() -> (Unit) = {}
	) : this(loritta, labels, descriptionKey, sourceTemplatePath, listOf(corner), builder)

	data class Corners(
		val upperLeftX: Float, val upperLeftY: Float,
		val upperRightX: Float, val upperRightY: Float,
		val lowerRightX: Float, val lowerRightY: Float,
		val lowerLeftX: Float, val lowerLeftY: Float
	)
}