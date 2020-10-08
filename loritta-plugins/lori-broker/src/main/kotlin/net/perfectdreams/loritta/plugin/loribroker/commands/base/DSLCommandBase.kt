package net.perfectdreams.loritta.plugin.loribroker.commands.base

import com.mrpowergamerbr.loritta.Loritta
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommand
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandBuilder
import net.perfectdreams.loritta.platform.discord.commands.discordCommand
import net.perfectdreams.loritta.plugin.loribroker.LoriBrokerPlugin

interface DSLCommandBase {
	fun command(plugin: LoriBrokerPlugin, loritta: Loritta): DiscordCommand

	fun create(loritta: Loritta, labels: List<String>, builder: DiscordCommandBuilder.() -> (Unit)): DiscordCommand {
		return discordCommand(
				loritta,
				this::class.simpleName!!,
				labels,
				CommandCategory.ECONOMY
		) {
			builder.invoke(this)
		}
	}
}