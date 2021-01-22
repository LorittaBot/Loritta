package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.utils.image.Image
import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.ImageAbstractCommandBase
import kotlin.math.max

class SAMCommand(m: RosbifePlugin) : ImageAbstractCommandBase(
		m.loritta,
		listOf("sam", "southamericamemes")
) {
	override fun command() = create {
		localizedDescription("commands.images.sam.description")
		localizedExamples(Command.SINGLE_IMAGE_EXAMPLES_KEY)

		usage {
			argument(ArgumentType.IMAGE) {}
		}

		needsToUploadFiles = true

		executes {
			val div = 1.5

			val image = validate(image(0))

			val height = (image.height / div).toInt() // Baseando na altura
			val seloSouthAmericaMemes = loritta.assets.loadImage("selo_sam.png", loadFromCache = true).getScaledInstance(height, height, Image.ScaleType.SMOOTH)

			val x = loritta.random.nextInt(0, max(1, image.width - seloSouthAmericaMemes.width))
			val y = loritta.random.nextInt(0, max(1, image.height - seloSouthAmericaMemes.height))

			image.createGraphics().drawImage(seloSouthAmericaMemes, x, y)

			sendImage(image, "south_america_memes.png")
		}
	}
}