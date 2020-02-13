package net.perfectdreams.loritta.commands.minecraft

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.*

interface DSLCommandBase {
	fun command(loritta: LorittaBot): Command<CommandContext>

	fun create(loritta: LorittaBot, labels: List<String>, builder: CommandBuilder<CommandContext>.() -> (Unit)): Command<CommandContext> {
		return command(
				loritta,
				this::class.simpleName!!,
				labels,
				CommandCategory.MINECRAFT
		) {
			needsToUploadFiles = true
			builder.invoke(this)
		}
	}
}