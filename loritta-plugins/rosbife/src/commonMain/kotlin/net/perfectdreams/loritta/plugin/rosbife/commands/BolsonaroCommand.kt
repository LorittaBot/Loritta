package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class BolsonaroCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
		m.loritta,
		listOf("bolsonaro", "bolsonarotv"),
		1,
		"commands.images.bolsonaro.description",
		"/api/v1/images/bolsonaro",
		"bolsonaro_tv.png",
)