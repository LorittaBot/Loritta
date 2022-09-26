package net.perfectdreams.loritta.legacy.commands.vanilla.images.base

import net.perfectdreams.loritta.legacy.api.LorittaBot
import net.perfectdreams.loritta.legacy.api.commands.LorittaAbstractCommandBase
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory

/**
 * Extends [LorittaAbstractCommandBase] and sets the [category] to [CommandCategory.IMAGES], simple command images in this plugin
 * should extend this class!
 */
abstract class ImageAbstractCommandBase(loritta: LorittaBot, labels: List<String>) : LorittaAbstractCommandBase(
		loritta,
		labels,
		CommandCategory.IMAGES
)