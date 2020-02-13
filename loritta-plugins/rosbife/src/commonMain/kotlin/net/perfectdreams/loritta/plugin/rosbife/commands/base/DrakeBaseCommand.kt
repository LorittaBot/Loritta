package net.perfectdreams.loritta.plugin.rosbife.commands.base

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.api.utils.image.Image

interface DrakeBaseCommand : DSLCommandBase {
	val descriptionKey: String
	val sourceTemplatePath: String

	override fun command(loritta: LorittaBot): Command<CommandContext>

	fun create(loritta: LorittaBot, labels: List<String>): Command<CommandContext> {
		return command(
				loritta,
				this::class.simpleName!!,
				labels,
				CommandCategory.IMAGES
		) {
			description { it[descriptionKey] }

			examples {
				listOf("@Loritta @MrPowerGamerBR")
			}

			usage {
				argument(ArgumentType.IMAGE) {}
				argument(ArgumentType.IMAGE) {}
			}

			executes {
				val bi = this.loritta.assets.loadImage(sourceTemplatePath) // Primeiro iremos carregar o nosso template
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

				sendImage(bi, sourceTemplatePath)
			}
		}
	}
}