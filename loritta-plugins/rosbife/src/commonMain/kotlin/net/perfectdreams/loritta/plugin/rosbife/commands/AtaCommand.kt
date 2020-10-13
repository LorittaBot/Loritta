package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

class AtaCommand(m: RosbifePlugin) : BasicSkewedImageCommand(
		m.loritta,
		listOf("ata"),
		"commands.images.ata.description",
		"ata.png",
		Corners(
				107F, 0F,
				300F, 0F,
				300F, 177F,
				96F, 138F
		)
)