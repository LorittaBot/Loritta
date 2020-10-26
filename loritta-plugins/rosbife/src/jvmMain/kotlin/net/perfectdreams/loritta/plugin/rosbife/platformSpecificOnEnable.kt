package net.perfectdreams.loritta.plugin.rosbife

// import net.perfectdreams.loritta.plugin.rosbife.commands.AtendenteCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.*

actual fun RosbifePlugin.platformSpecificOnEnable() {
	registerCommands(
			// AtendenteCommand.command(loritta),
			ToBeContinuedCommand(this),
			TerminatorCommand(this),
			MorrePragaCommand(this),
			CarlyAaahCommand(this),
			PetPetCommand(this),
			EdnaldoTvCommand(this),
			EdnaldoBandeiraCommand(this)
	)
}