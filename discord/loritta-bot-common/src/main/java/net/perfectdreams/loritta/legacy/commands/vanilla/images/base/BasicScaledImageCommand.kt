package net.perfectdreams.loritta.legacy.commands.vanilla.images.base

import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.api.commands.CommandBuilder
import net.perfectdreams.loritta.common.api.commands.CommandContext
import net.perfectdreams.loritta.common.utils.createImage
import net.perfectdreams.loritta.common.utils.image.Image
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils

open class BasicScaledImageCommand(
	loritta: LorittaBot,
	labels: List<String>,
	descriptionKey: String,
	sourceTemplatePath: String,
	val scaleXTo: Int,
	val scaleYTo: Int,
	val x: Int,
	val y: Int,
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

			val scaled = contextImage.getScaledInstance(scaleXTo, scaleYTo, Image.ScaleType.SMOOTH)

			graphics.drawImage(scaled, x, y)
			graphics.drawImage(template, 0, 0)

			sendImage(base, sourceTemplatePath)
		}
	},
	slashCommandName
)