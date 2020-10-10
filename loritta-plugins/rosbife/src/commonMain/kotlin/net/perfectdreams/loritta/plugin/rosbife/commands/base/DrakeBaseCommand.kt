package net.perfectdreams.loritta.plugin.rosbife.commands.base

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.api.utils.image.Image

interface DrakeBaseCommand : DSLCommandBase {
	val descriptionKey: String
	val sourceTemplatePath: String
	val scale: Int

	override fun command(loritta: LorittaBot): Command<CommandContext>

	fun create(loritta: LorittaBot, labels: List<String>): Command<CommandContext> {
		return command(
				loritta,
				this::class.simpleName!!,
				labels,
				CommandCategory.IMAGES
		) {
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
}