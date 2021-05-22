package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class CarlyAaahCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
		m.loritta,
		listOf("carlyaaah"),
		1,
		"commands.command.carlyaaah.description",
		"/api/v1/videos/carly-aaah",
		"carly_aaah.mp4",
		sendTypingStatus = true,
		category = CommandCategory.VIDEOS
)