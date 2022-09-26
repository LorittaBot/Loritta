package net.perfectdreams.loritta.legacy.commands.vanilla.images.base

import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.api.commands.LorittaAbstractCommandBase
import net.perfectdreams.loritta.common.commands.CommandCategory

/**
 * Extends [LorittaAbstractCommandBase] and sets the [category] to [CommandCategory.IMAGES], simple command images in this plugin
 * should extend this class!
 */
abstract class ImageAbstractCommandBase(loritta: LorittaBot, labels: List<String>) : LorittaAbstractCommandBase(
		loritta,
		labels,
		net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES
)