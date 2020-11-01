package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class ArtCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
		m.loritta,
		listOf("art", "arte"),
		1,
		"commands.images.art.description",
		"/api/v1/images/art",
		"art.png"
)