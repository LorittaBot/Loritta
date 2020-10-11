package net.perfectdreams.loritta.plugin.rosbife.commands.base

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.utils.image.Image

abstract class DrakeBaseCommand(
		loritta: LorittaBot,
		labels: List<String>,
		val descriptionKey: String,
		val sourceTemplatePath: String,
		val scale: Int
) : ImageAbstractCommandBase(loritta, labels) {
	override fun command() = create {
		localizedDescription(descriptionKey)

		examples {
			+ "@Loritta @MrPowerGamerBR"
		}

		usage {
			argument(ArgumentType.IMAGE) {}
			argument(ArgumentType.IMAGE) {}
		}

		similarCommands = listOf(
				"DrakeCommand",
				"BolsoDrakeCommand",
				"LoriDrakeCommand"
		)

		executes {
			val bi = this.loritta.assets.loadImage(sourceTemplatePath) // Primeiro iremos carregar o nosso template
			val graph = bi.createGraphics()

			run {
				val avatarImg = validate(image(0))
				val image = avatarImg.getScaledInstance(150 * scale, 150 * scale, Image.ScaleType.SMOOTH)
				graph.drawImage(image, 150 * scale, 0)
			}

			run {
				val avatarImg = validate(image(1))
				val image = avatarImg.getScaledInstance(150 * scale, 150 * scale, Image.ScaleType.SMOOTH)
				graph.drawImage(image, 150 * scale, 150 * scale)
			}

			sendImage(bi, sourceTemplatePath)
		}
	}
}