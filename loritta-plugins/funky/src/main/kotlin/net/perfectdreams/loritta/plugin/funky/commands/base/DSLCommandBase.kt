package net.perfectdreams.loritta.plugin.funky.commands.base

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommand
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandBuilder
import net.perfectdreams.loritta.platform.discord.commands.discordCommand
import net.perfectdreams.loritta.plugin.funky.FunkyPlugin

interface DSLCommandBase {
	fun command(loritta: LorittaBot, m: FunkyPlugin): Command<CommandContext>

	fun create(loritta: LorittaBot, labels: List<String>, builder: DiscordCommandBuilder.() -> (Unit)): DiscordCommand {
		return discordCommand(
				loritta as LorittaDiscord,
				this::class.simpleName!!,
				labels,
				CommandCategory.MUSIC
		) {
			canUseInPrivateChannel = false
			
			builder.invoke(this)
		}
	}
}