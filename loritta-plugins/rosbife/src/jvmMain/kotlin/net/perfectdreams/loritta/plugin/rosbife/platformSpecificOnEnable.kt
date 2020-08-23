package net.perfectdreams.loritta.plugin.rosbife

// import net.perfectdreams.loritta.plugin.rosbife.commands.AtendenteCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.CarlyAaahCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.TerminatorCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.ToBeContinuedCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.MorrePragaCommand

actual fun RosbifePlugin.platformSpecificOnEnable() {
	registerCommands(
			// AtendenteCommand.command(loritta),
			ToBeContinuedCommand.command(loritta),
			TerminatorCommand.command(loritta),
			MorrePragaCommand.command(loritta),
			CarlyAaahCommand.command(loritta)
	)
}