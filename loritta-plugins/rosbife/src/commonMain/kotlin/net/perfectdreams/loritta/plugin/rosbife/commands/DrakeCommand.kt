package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.utils.image.Image

object DrakeCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot) = create(
			loritta,
			listOf("drake")
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