package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageCommandBase

class EdnaldoBandeiraCommand(m: RosbifePlugin) : GabrielaImageCommandBase(
		m.loritta,
		listOf("ednaldobandeira"),
		"commands.images.ednaldobandeira.description",
		"/api/images/ednaldo-bandeira",
		"ednaldo_bandeira.png"
)
