package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.common.api.commands.ArgumentType
import net.perfectdreams.loritta.common.api.commands.Command
import net.perfectdreams.loritta.common.utils.image.Image
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.ImageAbstractCommandBase
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils
import kotlin.math.max

class SAMCommand(m: LorittaDiscord) : ImageAbstractCommandBase(
		m,
		listOf("sam", "southamericamemes")
) {
	override fun command() = create {
		localizedDescription("commands.command.sam.description")
		localizedExamples(Command.SINGLE_IMAGE_EXAMPLES_KEY)

		usage {
			argument(ArgumentType.IMAGE) {}
		}

		needsToUploadFiles = true

		executes {
			OutdatedCommandUtils.sendOutdatedCommandMessage(this, locale, "brmemes sam")

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