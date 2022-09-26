package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase

class EmojiFightCommand(val m: LorittaBot) : DiscordAbstractCommandBase(
		m,
		listOf("emojifight", "rinhadeemoji", "emotefight"),
		net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY
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
					this,
					null,
					maxPlayersInEvent
			)

			emojiFight.start()
		}
	}
}