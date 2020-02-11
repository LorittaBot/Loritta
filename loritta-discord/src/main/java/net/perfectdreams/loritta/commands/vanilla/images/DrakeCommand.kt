package net.perfectdreams.loritta.commands.vanilla.images

import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.command
import net.perfectdreams.loritta.api.utils.image.Image
import net.perfectdreams.loritta.platform.discord.LorittaDiscord

class DrakeCommand {
	fun create(loritta: LorittaDiscord) = command(
			loritta,
			"DrakeCommand",
			listOf("drake2"),
			CommandCategory.IMAGES
	) {
		description { it["commands.images.drake.description"] }
		examples { listOf("@MrPowerGamerBR @Loritta") }

		usage {
			argument(ArgumentType.IMAGE) {}
			argument(ArgumentType.IMAGE) {}
		}

		needsToUploadFiles = true

		executes {
			val bi = this.loritta.assets.loadImage("drake.png") // Primeiro iremos carregar o nosso template
			val graph = bi.createGraphics()

			run {
				val avatarImg = validate(image(0))
				val image = avatarImg.getScaledInstance(150, 150, Image.ScaleType.SMOOTH)
				graph.drawImage(image, 150, 0)
			}

			run {
				val avatarImg = validate(image(1))
				val image = avatarImg.getScaledInstance(150, 150, Image.ScaleType.SMOOTH)
				graph.drawImage(image, 150, 150)
			}

			sendImage(bi, "drake.png")
		}
	}
}