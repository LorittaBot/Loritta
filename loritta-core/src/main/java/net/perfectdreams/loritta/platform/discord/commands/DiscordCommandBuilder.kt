package net.perfectdreams.loritta.platform.discord.commands

import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.CommandBuilder
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.platform.discord.LorittaDiscord

fun discordCommand(loritta: LorittaDiscord, commandName: String, labels: List<String>, builder: DiscordCommandBuilder.() -> (Unit)): DiscordCommand {
	val b = DiscordCommandBuilder(loritta, commandName, labels)
	builder.invoke(b)
	return b.buildDiscord()
}

class DiscordCommandBuilder(
		val lorittaDiscord: LorittaDiscord,
		commandName: String,
		labels: List<String>
) : CommandBuilder<CommandContext>(lorittaDiscord, commandName, labels) {
	var userRequiredPermissions = listOf<Permission>()
	var botRequiredPermissions = listOf<Permission>()
	var requiresMusic = false

	fun buildDiscord(): DiscordCommand {
		val usage = arguments {
			usageCallback?.invoke(this)
		}

		return DiscordCommand(
				lorittaDiscord = lorittaDiscord,
				commandName = commandName,
				labels = labels,
				description = descriptionCallback ?: { "???" },
				usage = usage,
				examples = examplesCallback,
				executor = executeCallback!!
		).apply { build2().invoke(this) }
				.also {
					it.userRequiredPermissions = userRequiredPermissions
					it.botRequiredPermissions = botRequiredPermissions
					it.requiresMusic = requiresMusic
				}
	}
}