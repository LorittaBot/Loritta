package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageCommandBase

class EdnaldoTvCommand(m: RosbifePlugin) : GabrielaImageCommandBase(
		m.loritta,
		listOf("ednaldotv"),
		"commands.images.ednaldotv.description",
		"/api/images/ednaldo-tv",
		"ednaldo_tv.png"
)
