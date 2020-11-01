package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class EdnaldoTvCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
		m.loritta,
		listOf("ednaldotv"),
		1,
		"commands.images.ednaldotv.description",
		"/api/v1/images/ednaldo-tv",
		"ednaldo_tv.png"
)
