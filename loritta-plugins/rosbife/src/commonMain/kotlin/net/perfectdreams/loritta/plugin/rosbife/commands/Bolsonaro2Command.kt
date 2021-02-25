package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class Bolsonaro2Command(m: RosbifePlugin) : GabrielaImageServerCommandBase(
		m.loritta,
		listOf("bolsonaro2", "bolsonarotv2"),
		1,
		"commands.command.bolsonaro.description",
		"/api/v1/images/bolsonaro2",
		"bolsonaro_tv2.png",
)