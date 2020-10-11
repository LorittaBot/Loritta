package net.perfectdreams.loritta.plugin.rosbife.commands.base

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandBuilder
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.utils.createImage
import net.perfectdreams.loritta.api.utils.image.Image

open class BasicScaledImageCommand(
		loritta: LorittaBot,
		labels: List<String>,
		descriptionKey: String,
		sourceTemplatePath: String,
		val scaleXTo: Int,
		val scaleYTo: Int,
		val x: Int,
		val y: Int,
		builder: CommandBuilder<CommandContext>.() -> (Unit) = {}
) : BasicImageCommand(
		loritta,
		labels,
		descriptionKey,
		sourceTemplatePath,
		{
			builder.invoke(this)

			executes {
				val contextImage = validate(image(0))
				val template = loritta.assets.loadImage(sourceTemplatePath, loadFromCache = true)

				val base = createImage(template.width, template.height)
				val graphics = base.createGraphics()

				val scaled = contextImage.getScaledInstance(scaleXTo, scaleYTo, Image.ScaleType.SMOOTH)

				graphics.drawImage(scaled, x, y)
				graphics.drawImage(template, 0, 0)

				sendImage(base, sourceTemplatePath)
			}
		}
)