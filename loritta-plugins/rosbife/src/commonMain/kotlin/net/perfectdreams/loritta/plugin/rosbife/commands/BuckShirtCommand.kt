package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class BuckShirtCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
		m.loritta,
		listOf("buckshirt", "buckcamisa"),
		1,
		"commands.images.buckshirt.description",
		"/api/v1/images/buck-shirt",
		"buck_shirt.png",
)