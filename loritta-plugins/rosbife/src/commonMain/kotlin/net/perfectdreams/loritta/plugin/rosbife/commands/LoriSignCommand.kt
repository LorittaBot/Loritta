package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class LoriSignCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
		m.loritta,
		listOf("lorisign", "lorittasign", "loriplaca", "lorittaplaca"),
		1,
		"commands.command.lorisign.description",
		"/api/v1/images/lori-sign",
		"lori_sign.png",
)