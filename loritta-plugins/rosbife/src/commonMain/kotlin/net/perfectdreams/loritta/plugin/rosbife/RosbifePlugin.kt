package net.perfectdreams.loritta.plugin.rosbife

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.plugin.LorittaPlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.ArtCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.AtaCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.AttackOnHeartCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.BobBurningPaperCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.BolsoDrakeCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.BolsoFrameCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.Bolsonaro2Command
import net.perfectdreams.loritta.plugin.rosbife.commands.BolsonaroCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.BriggsCoverCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.BuckShirtCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.CanellaDvdCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.CarlyAaahCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.ChicoAtaCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.DrakeCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.EdnaldoBandeiraCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.EdnaldoTvCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.GessyAtaCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.LoriAtaCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.LoriDrakeCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.LoriSignCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.PassingPaperCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.PepeDreamCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.PetPetCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.QuadroCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.RipTvCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.RomeroBrittoCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.SAMCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.StudiopolisTvCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.SustoCommand

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
				RomeroBrittoCommand(this),
				SAMCommand(this),
				StudiopolisTvCommand(this),
				SustoCommand(this),
				CarlyAaahCommand(this),
				PetPetCommand(this),
				EdnaldoTvCommand(this),
				EdnaldoBandeiraCommand(this),
				RipTvCommand(this),
				AttackOnHeartCommand(this)
		)
		platformSpecificOnEnable()
	}
}