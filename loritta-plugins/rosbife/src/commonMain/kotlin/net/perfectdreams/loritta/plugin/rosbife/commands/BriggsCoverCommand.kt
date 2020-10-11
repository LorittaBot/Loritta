package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

class BriggsCoverCommand(m: RosbifePlugin) : BasicSkewedImageCommand(
		m.loritta,
		listOf("briggscover", "coverbriggs", "capabriggs", "briggscapa"),
		"commands.images.briggscover.description",
		"briggs_capa.png",
		Corners(
				242F,67F, // UL
				381F,88F, // UR
				366F,266F, // LR
				218F, 248F // LL
		)
)