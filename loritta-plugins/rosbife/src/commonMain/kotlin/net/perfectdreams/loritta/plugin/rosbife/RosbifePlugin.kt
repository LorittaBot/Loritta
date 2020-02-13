package net.perfectdreams.loritta.plugin.rosbife

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.plugin.LorittaPlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.*

class RosbifePlugin(name: String, loritta: LorittaBot) : LorittaPlugin(name, loritta) {
	override fun onEnable() {
		registerCommands(
				ArtCommand.command(loritta),
				AtaCommand.command(loritta),
				BobBurningPaperCommand.command(loritta),
				BolsoDrakeCommand.command(loritta),
				Bolsonaro2Command.command(loritta),
				BolsonaroCommand.command(loritta),
				BriggsCoverCommand.command(loritta),
				BuckShirtCommand.command(loritta),
				CanellaDvdCommand.command(loritta),
				ChicoAtaCommand.command(loritta),
				DrakeCommand.command(loritta),
				GessyAtaCommand.command(loritta),
				LoriAtaCommand.command(loritta),
				LoriSignCommand.command(loritta),
				PassingPaperCommand.command(loritta),
				PepeDreamCommand.command(loritta),
				QuadroCommand.command(loritta),
				RomeroBrittoCommands.command(loritta),
				SAMCommand.command(loritta),
				StudiopolisTvCommand.command(loritta),
				SustoCommand.command(loritta)
		)
		platformSpecificOnEnable()
	}
}