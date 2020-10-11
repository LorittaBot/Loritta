package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

class Bolsonaro2Command(m: RosbifePlugin) : BasicSkewedImageCommand(
		m.loritta,
		listOf("bolsonaro2", "bolsonarotv2"),
		"commands.images.bolsonaro.description",
		"bolsonaro_tv2.png",
		Corners(
				213F,34F,
				435F,40F,
				430F,166F,
				212F, 161F
		)
)