package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

class BolsonaroCommand(m: RosbifePlugin) : BasicSkewedImageCommand(
		m.loritta,
		listOf("bolsonaro", "bolsonarotv"),
		"commands.images.bolsonaro.description",
		"bolsonaro_tv.png",
		Corners(
				108F,11F,
				383F,8F,
				375F,167F,
				106F, 158F
		)
)