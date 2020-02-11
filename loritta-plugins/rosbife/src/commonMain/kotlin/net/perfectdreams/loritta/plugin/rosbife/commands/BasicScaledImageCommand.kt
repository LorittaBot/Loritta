package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandBuilder
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.utils.createImage
import net.perfectdreams.loritta.api.utils.image.Image

interface BasicScaledImageCommand : BasicImageCommand {
	val scaleXTo: Int
	val scaleYTo: Int
	val x: Int
	val y: Int
	val sourceTemplatePath: String

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

				val scaled = contextImage.getScaledInstance(scaleXTo, scaleYTo, Image.ScaleType.SMOOTH)

				graphics.drawImage(scaled, x, y)
				graphics.drawImage(template, 0, 0)

				sendImage(base, sourceTemplatePath)
			}
		}
	}
}