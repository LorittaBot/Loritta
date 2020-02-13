package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite.base

import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandBuilder
import net.perfectdreams.loritta.platform.discord.commands.discordCommand
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff

interface DSLCommandBase {
	fun command(loritta: LorittaDiscord, m: FortniteStuff): Command<CommandContext>

	fun create(loritta: LorittaDiscord, labels: List<String>, builder: DiscordCommandBuilder.() -> (Unit)): Command<CommandContext> {
		return discordCommand(
				loritta,
				this::class.simpleName!!,
				labels,
				CommandCategory.FORTNITE
		) {
			this.needsToUploadFiles = true

			builder.invoke(this)
		}
	}
}