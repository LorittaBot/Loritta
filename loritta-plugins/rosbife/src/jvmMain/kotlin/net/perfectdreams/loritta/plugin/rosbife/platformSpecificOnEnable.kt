package net.perfectdreams.loritta.plugin.rosbife

// import net.perfectdreams.loritta.plugin.rosbife.commands.AtendenteCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.TerminatorCommand
import net.perfectdreams.loritta.plugin.rosbife.commands.ToBeContinuedCommand

actual fun RosbifePlugin.platformSpecificOnEnable() {
	registerCommands(
			// AtendenteCommand.command(loritta),
			ToBeContinuedCommand.command(loritta),
			TerminatorCommand.command(loritta)
	)
}