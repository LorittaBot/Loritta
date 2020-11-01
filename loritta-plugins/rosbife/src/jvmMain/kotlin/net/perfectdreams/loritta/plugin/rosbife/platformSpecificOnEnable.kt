package net.perfectdreams.loritta.plugin.rosbife

// import net.perfectdreams.loritta.plugin.rosbife.commands.AtendenteCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.MorrePragaCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.TerminatorCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.ToBeContinuedCommand

actual fun RosbifePlugin.platformSpecificOnEnable() {
	registerCommands(
			// AtendenteCommand.command(loritta),
			ToBeContinuedCommand(this),
			TerminatorCommand(this),
			MorrePragaCommand(this)
	)
}