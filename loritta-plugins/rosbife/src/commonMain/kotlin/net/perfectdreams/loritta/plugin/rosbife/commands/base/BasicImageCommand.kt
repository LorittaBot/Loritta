package net.perfectdreams.loritta.plugin.rosbife.commands.base

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandBuilder
import net.perfectdreams.loritta.api.commands.CommandContext

open class BasicImageCommand(
		loritta: LorittaBot,
		labels: List<String>,
		val descriptionKey: String,
		val sourceTemplatePath: String,
		val builder: CommandBuilder<CommandContext>.() -> (Unit)
) : ImageAbstractCommandBase(
		loritta,
		labels
) {
	override fun command() = create {
		localizedDescription(descriptionKey)

		usage {
			argument(ArgumentType.IMAGE) {}
		}

		needsToUploadFiles = true

		builder.invoke(this)
	}
}