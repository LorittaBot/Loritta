package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageCommandBase

class CarlyAaahCommand(m: RosbifePlugin) : GabrielaImageCommandBase(
		m.loritta,
		listOf("carlyaaah"),
		"commands.images.carlyaaah.description",
		"/api/videos/carly-aaah",
		"carly_aaah.mp4"
)