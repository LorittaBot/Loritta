package net.perfectdreams.loritta.plugin.helpinghands.commands.base

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.plugin.helpinghands.HelpingHandsPlugin

interface DSLCommandBase {
	fun command(plugin: HelpingHandsPlugin, loritta: LorittaBot): Command<CommandContext>

	fun create(loritta: LorittaBot, labels: List<String>, builder: CommandBuilder<CommandContext>.() -> (Unit)): Command<CommandContext> {
		return command(
				loritta,
				this::class.simpleName!!,
				labels,
				CommandCategory.ECONOMY
		) {
			builder.invoke(this)
		}
	}
}