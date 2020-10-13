package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

class BolsoFrameCommand(m: RosbifePlugin) : BasicSkewedImageCommand(
		m.loritta,
		listOf("bolsoframe", "bolsonaroframe", "bolsoquadro", "bolsonaroquadro"),
		"commands.images.bolsoframe.description",
		"bolsoframe.png",
		listOf(
				Corners(
						314F, 36F,
						394F, 41F,
						385F, 156F,
						301F, 151F
				)
		)
)