package net.perfectdreams.loritta.plugin.rosbife.commands.base

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.LorittaAbstractCommandBase
import net.perfectdreams.loritta.common.commands.CommandCategory

/**
 * Extends [LorittaAbstractCommandBase] and sets the [category] to [CommandCategory.IMAGES], simple command images in this plugin
 * should extend this class!
 */
abstract class ImageAbstractCommandBase(loritta: LorittaBot, labels: List<String>) : LorittaAbstractCommandBase(
		loritta,
		labels,
		CommandCategory.IMAGES
)