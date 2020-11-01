package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class EdnaldoBandeiraCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
		m.loritta,
		listOf("ednaldobandeira"),
		1,
		"commands.images.ednaldobandeira.description",
		"/api/v1/images/ednaldo-bandeira",
		"ednaldo_bandeira.png"
)
