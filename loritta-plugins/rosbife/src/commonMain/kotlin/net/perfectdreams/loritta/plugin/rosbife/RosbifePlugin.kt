package net.perfectdreams.loritta.plugin.rosbife

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.plugin.LorittaPlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.*

class RosbifePlugin(name: String, loritta: LorittaBot) : LorittaPlugin(name, loritta) {
	override fun onEnable() {
		registerCommands(
				AtaCommand.command(loritta),
				ArtCommand.command(loritta),
				BobBurningPaperCommand.command(loritta),
				Bolsonaro2Command.command(loritta),
				BolsonaroCommand.command(loritta),
				BriggsCoverCommand.command(loritta),
				BuckShirtCommand.command(loritta),
				CanellaDvdCommand.command(loritta),
				ChicoAtaCommand.command(loritta),
				DrakeCommand.command(loritta),
				PepeDreamCommand.command(loritta),
				QuadroCommand.command(loritta),
				RomeroBrittoCommands.command(loritta),
				StudiopolisTvCommand.command(loritta),
				SustoCommand.command(loritta)
		)
		platformSpecificOnEnable()
	}
}