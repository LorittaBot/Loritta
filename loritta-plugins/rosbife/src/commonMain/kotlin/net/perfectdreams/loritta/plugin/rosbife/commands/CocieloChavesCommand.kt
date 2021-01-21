package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class CocieloChavesCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
		m.loritta,
		listOf("cocielochaves"),
		5,
		"commands.images.cocielochaves.description",
		"/api/v1/videos/cocielo-chaves",
		"cocielo_chaves.mp4",
		sendTypingStatus = true
)