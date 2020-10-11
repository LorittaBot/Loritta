package net.perfectdreams.loritta.plugin.helpinghands.commands

import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.helpinghands.HelpingHandsPlugin
import net.perfectdreams.loritta.plugin.helpinghands.utils.EmojiFight

class EmojiFightCommand(val plugin: HelpingHandsPlugin) : DiscordAbstractCommandBase(
		plugin.loritta,
		listOf("emojifight", "rinhadeemoji"),
		CommandCategory.ECONOMY
) {
	override fun command() = create {
		localizedDescription("commands.economy.emojifight.description")

		examples {
			+ "100"
			+ "1000"
		}

		usage {
			arguments {
				argument(ArgumentType.NUMBER) {}
			}
		}

		this.similarCommands = listOf("EmojiFightBetCommand")
		this.canUseInPrivateChannel = false

		executesDiscord {
			val emojiFight = EmojiFight(
					plugin,
					this,
					null
			)

			emojiFight.start()
		}
	}
}