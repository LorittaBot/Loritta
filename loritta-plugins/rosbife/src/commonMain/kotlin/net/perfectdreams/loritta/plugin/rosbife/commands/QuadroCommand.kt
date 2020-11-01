package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class QuadroCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
		m.loritta,
		listOf("quadro", "frame", "picture", "wolverine"),
		1,
		"commands.images.wolverine.description",
		"/api/v1/images/wolverine-frame",
		"wolverine_frame.png",
)