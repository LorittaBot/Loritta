package net.perfectdreams.loritta.plugin.rosbife.commands.base

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.*

interface BasicImageCommand {
	val descriptionKey: String
	val sourceTemplatePath: String

	fun command(loritta: LorittaBot): Command<CommandContext>

	fun create(loritta: LorittaBot, labels: List<String>, builder: CommandBuilder<CommandContext>.() -> (Unit)): Command<CommandContext> {
		return command(
				loritta,
				this::class.simpleName!!,
				labels,
				CommandCategory.IMAGES
		) {
			localizedDescription(descriptionKey)

			usage {
				argument(ArgumentType.IMAGE) {}
			}

			needsToUploadFiles = true

			builder.invoke(this)
		}
	}
}