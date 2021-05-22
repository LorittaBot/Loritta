package net.perfectdreams.loritta.plugin.helpinghands.commands

import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.helpinghands.HelpingHandsPlugin
import net.perfectdreams.loritta.plugin.helpinghands.utils.EmojiFight

class EmojiFightCommand(val plugin: HelpingHandsPlugin) : DiscordAbstractCommandBase(
		plugin.loritta,
		listOf("emojifight", "rinhadeemoji", "emotefight"),
		CommandCategory.ECONOMY
) {
	override fun command() = create {
		localizedDescription("commands.command.emojifight.description")

		usage {
			arguments {
				argument(ArgumentType.NUMBER) {
					optional = true
				}
			}
		}

		this.similarCommands = listOf("EmojiFightBetCommand")
		this.canUseInPrivateChannel = false

		executesDiscord {
			val maxPlayersInEvent = (
					(this.args.getOrNull(0) ?.toIntOrNull() ?: EmojiFight.DEFAULT_MAX_PLAYER_COUNT)
							.coerceIn(2, EmojiFight.DEFAULT_MAX_PLAYER_COUNT)
			)

			val emojiFight = EmojiFight(
					plugin,
					this,
					null,
					maxPlayersInEvent
			)

			emojiFight.start()
		}
	}
}