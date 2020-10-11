package net.perfectdreams.loritta.plugin.rosbife

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.plugin.LorittaPlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.*

class RosbifePlugin(name: String, loritta: LorittaBot) : LorittaPlugin(name, loritta) {
	override fun onEnable() {
		registerCommands(
				ArtCommand(this),
				AtaCommand(this),
				BobBurningPaperCommand(this),
				BolsoDrakeCommand(this),
				BolsoFrameCommand(this),
				Bolsonaro2Command(this),
				BolsonaroCommand(this),
				BriggsCoverCommand(this),
				BuckShirtCommand(this),
				CanellaDvdCommand(this),
				ChicoAtaCommand(this),
				DrakeCommand(this),
				GessyAtaCommand(this),
				LoriAtaCommand(this),
				LoriDrakeCommand(this),
				LoriSignCommand(this),
				PassingPaperCommand(this),
				PepeDreamCommand(this),
				QuadroCommand(this),
				RomeroBrittoCommands(this),
				SAMCommand(this),
				StudiopolisTvCommand(this),
				SustoCommand(this)
		)
		platformSpecificOnEnable()
	}
}