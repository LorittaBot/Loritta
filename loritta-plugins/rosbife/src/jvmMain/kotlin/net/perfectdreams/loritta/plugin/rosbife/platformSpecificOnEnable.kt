package net.perfectdreams.loritta.plugin.rosbife

// import net.perfectdreams.loritta.plugin.rosbife.commands.AtendenteCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.*

actual fun RosbifePlugin.platformSpecificOnEnable() {
	registerCommands(
			// AtendenteCommand.command(loritta),
			ToBeContinuedCommand.command(loritta),
			TerminatorCommand.command(loritta),
			MorrePragaCommand.command(loritta),
			CarlyAaahCommand.command(loritta),
			PetPetCommand.command(loritta)
	)
}